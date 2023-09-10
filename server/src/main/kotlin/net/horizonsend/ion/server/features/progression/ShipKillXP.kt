package net.horizonsend.ion.server.features.progression

import github.scarsz.discordsrv.DiscordSRV
import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel
import net.horizonsend.ion.common.Colors
import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.common.database.cache.nations.RelationCache
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.achievements.Achievement
import net.horizonsend.ion.server.features.achievements.rewardAchievement
import net.horizonsend.ion.server.features.misc.CombatNPCKillEvent
import net.horizonsend.ion.server.features.starship.PilotedStarships.getDisplayNameComponent
import net.horizonsend.ion.server.features.starship.PilotedStarships.getRawDisplayName
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.damager.PlayerDamager
import net.horizonsend.ion.server.features.starship.damager.PlayerDamagerWrapper
import net.horizonsend.ion.server.features.starship.event.StarshipExplodeEvent
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.get
import net.horizonsend.ion.server.miscellaneous.utils.plainText
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.Bukkit.getServer
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.PlayerDeathEvent
import java.time.Instant
import java.util.UUID
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.pow
import kotlin.math.sqrt

object ShipKillXP : IonServerComponent() {
	data class ShipDamageData(
		val points: AtomicInteger = AtomicInteger(),
		var lastDamaged: Long = System.currentTimeMillis()
	)

	val damagerExpiration = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(5)

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	fun onStarshipExplode(event: StarshipExplodeEvent) {
		val arena = event.starship.world.name.lowercase(Locale.getDefault()).contains("arena")

		onShipKill(event.starship, event.starship.controller.getDisplayName().plainText(), arena)
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	fun onCombatNPCKill(event: CombatNPCKillEvent) {
		val arena = IonServer.configuration.serverName.equals("creative", ignoreCase = true)

		onPlayerKilled(event.id, event.name, event.killer, arena)
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	fun onPlayerDeath(event: PlayerDeathEvent) {
		val player: Player = event.entity
		val killer: Player? = player.killer
		val arena = IonServer.configuration.serverName.equals("creative", ignoreCase = true)

		onPlayerKilled(player.uniqueId, player.name, killer, arena)
	}

	private fun onPlayerKilled(killed: UUID, killedName: String, killer: Entity?, arena: Boolean) {
		val killedStarship = ActiveStarships.findByPilot(killed) ?: return

		if (killer is Player) {
			val starship = ActiveStarships.findByPassenger(killer) ?: return
			val damager = PlayerDamagerWrapper(killer, starship)

			val data = killedStarship.damagers.getOrPut(damager) { ShipDamageData(AtomicInteger(), System.currentTimeMillis()) }

			data.points.incrementAndGet()
			data.lastDamaged = System.currentTimeMillis()
		}

		onShipKill(killedStarship, killedName, arena)
	}

	private fun onShipKill(starship: ActiveStarship, killedPilotName: String, arena: Boolean) {
		IonServer.slF4JLogger.info(
			"ship killed at ${starship.centerOfMass}. " +
				"Pilot: ${starship.controller}. " +
				"Damagers: ${starship.damagers}"
		)

		val dataMap = starship.damagers
			.filter { (damager, _) ->
				if (damager !is PlayerDamager) return@filter false

				// require they be online to get xp
				// if they have this perm, e.g. someone in dutymode or on creative, they don't get xp
				return@filter !damager.player.hasPermission("starships.noxp")
			}
			.toMutableMap()

		val killedName: Component? = (starship as? ActiveControlledStarship)?.let { getDisplayNameComponent(it.data) }

		processDamagers(starship, dataMap)
		shipKillMessage(killedPilotName, killedName, starship.type, starship.damagers, arena)
	}

	private fun processDamagers(
		starship: ActiveStarship,
		dataMap: MutableMap<Damager, ShipDamageData> // Filtered
	) {
		val sum = dataMap.values.sumOf { it.points.get() }

		for ((damager, data) in dataMap.entries) {
			val (points, timeStamp) = data

			if (timeStamp < damagerExpiration) continue

			val player = (damager as? PlayerDamager)?.player ?: continue // shouldn't happen
			val killedSize = starship.initialBlockCount.toDouble()

			val pilotNation = SLPlayer[player].nation

			val killedPlayer: Player? = (starship.controller as? PlayerController)?.player
			val killedNation = killedPlayer?.let { SLPlayer[it].nation }

			if (pilotNation != null && killedNation != null) {
				if (RelationCache[pilotNation, killedNation].ordinal >= 5) {
					data.map.remove(damager)
					continue
				}
			}

			val percent = points.get() / sum
			val xp = ((sqrt(killedSize.pow(2.0) / sqrt(killedSize * 0.00005))) * percent).toInt()

			if (xp > 0) {
				SLXP.addAsync(player, xp)
				log.info("Gave ${player.name} $xp XP for ship-killing ${starship.controller.pilotName.plainText()}")
			}

			if (points.get() > 0 && player.uniqueId != killedPlayer?.uniqueId) player.rewardAchievement(Achievement.KILL_SHIP)
		}
	}

	private fun shipKillMessage(
		sunkPilotName: String,
		sunkShipName: Component?,
		sunkType: StarshipType,
		data: MutableMap<Damager, ShipDamageData>,
		arena: Boolean
	) {
		val descending = data.toList().sortedByDescending { it.second.lastDamaged }.toMutableList()
		val alertFeedbackColor = TextColor.color(Colors.ALERT)

		if (descending.isEmpty()) return

		// Begin killed ship formatting
		val killedShipName = sunkShipName ?: text("A ").color(alertFeedbackColor).append(sunkType.component)

		val killedNationColor = SLPlayer.findIdByName(sunkPilotName)
			?.let { SLPlayer[it]?.nation?.let { nationID -> NationCache[nationID].color } }
			?: 16777215 // white // So many null checks, meh, it's not called too often.

		val killedShipHover = text()
			.append(text(data.size).color(NamedTextColor.WHITE))
			.append(text(" block ").color(NamedTextColor.WHITE))
			.append(sunkType.component)
			.build()
			.asHoverEvent()
		// End killed ship formatting

		// Begin killer ship formatting
		val killer = descending.first().first; descending.removeFirst() // remove to prevent duplicate during assists
		val killerShip = killer.starship

		val killerMessage: Component = killerShip?.let {
			val message = text()

			val killerShipName = (killerShip as? ActiveControlledStarship)?.let { getDisplayNameComponent(it.data) }
				?: text("a ").color(alertFeedbackColor).append(killerShip.type.component)

			val killerNationColor = (killer as? PlayerDamager)?.let {
				SLPlayer[it.player].nation?.let {
					nationID -> NationCache[nationID].color
				}
			} ?: 16777215// white

			val killerShipHover = text()
				.append(text(killerShip.initialBlockCount).color(NamedTextColor.WHITE))
				.append(text(" block ").color(NamedTextColor.WHITE))
				.append(killerShip.type.component)
				.build()
				.asHoverEvent()
			// End killer ship formatting

			message
				.append(killer.getDisplayName().color(TextColor.color(killerNationColor)).hoverEvent(killerShipHover))
				.append(text(" piloting ").color(alertFeedbackColor).hoverEvent(killerShipHover))
				.append(killerShipName.hoverEvent(killerShipHover))
				.build()
		} ?: killer.getDisplayName()


		// Begin message
		val message = text()

		if (arena) {
			message
				.append(text("[").asComponent().color(TextColor.color(85, 85, 85)))
				.append(text("Space Arena").asComponent().color(TextColor.color(255, 255, 102)))
				.append(text("] ").asComponent().color(TextColor.color(85, 85, 85)))
		}

		message
			.append(killedShipName.hoverEvent(killedShipHover))
			.append(text(" piloted by ").color(alertFeedbackColor).hoverEvent(killedShipHover))
			.append(text(sunkPilotName).color(TextColor.color(killedNationColor)).hoverEvent(killedShipHover))
			.append(text(" was sunk by ").color(alertFeedbackColor))
			.append(killerMessage)

		if (data.size > 1) {
			message.append(text(", assisted by: ").color(alertFeedbackColor))

			var remainingAssists = data.size

			for ((damager, _) in descending) {
				val assistShip = damager.starship
				val assistNationColor = (damager as? PlayerDamager)?.let { playerDamager ->
					SLPlayer[playerDamager.player].nation?.let { nationID ->
						NationCache[nationID].color
					}
				} ?: 16777215 // white

				message
					.append(Component.newline())
					.append(damager.getDisplayName().color(TextColor.color(assistNationColor)))

				if (assistShip != null) {
					val assistShipName = (assistShip as? ActiveControlledStarship)?.let { getDisplayNameComponent(assistShip.data) }
					val assistHoverEvent = text()
						.append(text(assistShip.initialBlockCount).color(NamedTextColor.WHITE))
						.append(text(" block ").color(NamedTextColor.GRAY))
						.append(assistShip.type.component)
						.build()
						.asHoverEvent()

					message
						.append(text(" piloting ").color(alertFeedbackColor).hoverEvent(assistHoverEvent))

					assistShipName?.let {
						message
							.append(it)
							.append(text(", a ").color(alertFeedbackColor))
					}

					message
						.append(text(assistShip.initialBlockCount).color(NamedTextColor.WHITE))
						.append(text(" block ").color(alertFeedbackColor))
						.append(assistShip.type.component)
						.hoverEvent(assistHoverEvent)
				}

				remainingAssists--

				if (remainingAssists > 1) message.append(text(",").color(alertFeedbackColor))
			}
		}

		// If its in space arena only notify creative, else notify survival
		if (arena) getServer().sendMessage(message.build()) else Notify.online(message.build())

		if (Bukkit.getPluginManager().isPluginEnabled("DiscordSRV") && !arena) {
			Tasks.async {
				val channel: TextChannel = DiscordSRV.getPlugin()
					.getDestinationTextChannelForGameChannelName("events") ?: return@async

				// Formatting the messages
				val sunkShipNameString = sunkShipName?.let { PlainTextComponentSerializer.plainText().serialize(it) }

				val killedShipDiscordName = sunkShipNameString?.let { it.replace("<[^>]*>".toRegex(), "") + ", a" } ?: " a"
				val killerShipDiscordName = (killerShip as? ActiveControlledStarship)?.data?.name?.let {
					it.replace("<[^>]*>".toRegex(), "") + ", a"
				} ?: " a"

				val discordMessage =
					"$killedShipDiscordName ${data.size} block ${sunkType.displayName}, piloted by $sunkPilotName, was shot down by " +
						if (killer.starship != null) { "${
							(killer.getDisplayName() as TextComponent).content()
						}, piloting $killerShipDiscordName ${killer.starship!!.initialBlockCount} block ${
							killerShip!!.type.displayName
						}."
						} else (killer.getDisplayName() as TextComponent).content()
				// end formatting

				val headURL = "https://minotar.net/avatar/$sunkPilotName"
				// end nice extras

				val embed = EmbedBuilder() // Build the embed
					.setTitle("Ship Kill") // Title at top
					.setTimestamp(Instant.now()) // Timestamp at the bottom
					.setColor(killedNationColor) // Color bar on the side is the killed player's nation's color
					.setThumbnail(headURL) // Head of the killed player
					.addField(MessageEmbed.Field(discordMessage, "", false))

				// Assists section
				if (data.size > 1) {
					var assists = "" // Build a string to put all the assists on newlines in the same field

					for ((assistDamager, _) in descending) {
						val name = (assistDamager.getDisplayName() as? TextComponent)?.content() ?: continue

						val assistShip = assistDamager.starship
						val assistShipName = if (assistDamager.starship !is ActiveControlledStarship)
							"piloting ${
								(assistShip as ActiveControlledStarship).data.name?.let {
									getRawDisplayName(assistShip.data) + ", a"
								} ?: "a"} ${assistDamager.starship!!} block ${assistShip.type.displayName}\n"
						else "\n"

						assists += "$name, $assistShipName"
					}

					embed.addField(MessageEmbed.Field("Assisted by:", assists, false))
				}
				// End assists

				channel.sendMessageEmbeds(embed.build()).queue()
			}
		}
	}
}
