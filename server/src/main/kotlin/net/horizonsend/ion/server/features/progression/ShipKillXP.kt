package net.horizonsend.ion.server.features.progression

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
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
import net.horizonsend.ion.server.features.starship.Damager
import net.horizonsend.ion.server.features.starship.PilotedStarships.getDisplayNameComponent
import net.horizonsend.ion.server.features.starship.PilotedStarships.getRawDisplayName
import net.horizonsend.ion.server.features.starship.PlayerDamager
import net.horizonsend.ion.server.features.starship.PlayerDamagerWrapper
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.event.StarshipExplodeEvent
import net.horizonsend.ion.server.features.starship.event.StarshipPilotedEvent
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.get
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Bukkit.getPlayer
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
	private val map: Cache<Damager, ShipDamageData> = CacheBuilder.newBuilder()
		.expireAfterWrite(5L, TimeUnit.MINUTES)
		.build()

	private data class ShipDamageData(
		val map: MutableMap<Damager, AtomicInteger>,
		val size: Int,
		val type: StarshipType,
		val name: String?
	)

	private fun data(starship: ActiveStarship): ShipDamageData {
		// needs to be a direct reference to the starship's damagers so it stays synchronized
		val map = starship.damagers
		val size = starship.initialBlockCount
		val type = starship.type
		val name = (starship as ActiveControlledStarship).data.name
		return ShipDamageData(map, size, type, name)
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	fun onStarshipPilot(event: StarshipPilotedEvent) {
		addPassengers(event.starship)
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	fun onStarshipExplode(event: StarshipExplodeEvent) {
		onShipKill(event.starship)
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

	private fun addPassengers(starship: ActiveStarship) {
		for (player in starship.onlinePassengers) {
			map.put(PlayerDamagerWrapper(player, starship), data(starship))
		}
	}

	private fun onPlayerKilled(killed: UUID, killedName: String, killer: Entity?, arena: Boolean) {
		val data = map.getIfPresent(killed) ?: return
		if (killer is Player) {
			ActiveStarships.findByPassenger(killer)?.let {
				val damager = PlayerDamagerWrapper(killer, it)

				data.map.getOrPut(damager) { AtomicInteger() }.incrementAndGet()
			}
		}
		onShipKill(killed, killedName, data, arena)
	}

	private fun onShipKill(starship: ActiveStarship) {
		IonServer.slF4JLogger.info("ship killed at ${starship.centerOfMass.x} ${starship.centerOfMass.y} ${starship.centerOfMass.z}")

		val data = data(starship)
		for (id in starship.passengerIDs) {
			val killedName = getPlayer(id)?.name ?: "UNKNOWN"
			val arena = IonServer.configuration.serverName.equals("creative", ignoreCase = true)
			onShipKill(id, killedName, data, arena)
		}
	}

	private fun onShipKill(killed: UUID, killedName: String, data: ShipDamageData, arena: Boolean) {
		val dataMap: Map<Damager, Int> = data.map
			.filterKeys { damager ->
			if (damager !is PlayerDamager) return@filterKeys false
			// require they be online to get xp
			// if they have this perm, e.g. someone in dutymode or on creative, they don't get xp
			!damager.player.hasPermission("starships.noxp")
			}
			.mapValues { it.value.get() }

		val sum = dataMap.values.sum().toDouble()

		processDamagers(dataMap, data, sum, killedName, killed)

		map.invalidate(killed)
		killMessage(killedName, data, arena)
	}

	private fun processDamagers(
		dataMap: Map<Damager, Int>,
		data: ShipDamageData,
		sum: Double,
		killedName: String,
		killed: UUID
	) {
		for ((damager, points) in dataMap.entries) {
			val player = (damager as? PlayerDamager)?.player ?: continue // shouldn't happen
			val killedSize = data.size.toDouble()

			val pilotNation = SLPlayer[player].nation
			val killedNation = SLPlayer[getPlayer(killedName)!!].nation

			if (pilotNation != null && killedNation != null) {
				if (RelationCache[pilotNation, killedNation].ordinal >= 5) {
					data.map.remove(damager)
					continue
				}
			}

			val percent = points / sum
			val xp = ((sqrt(killedSize.pow(2.0) / sqrt(killedSize * 0.00005))) * percent).toInt()

			if (xp > 0) {
				SLXP.addAsync(player, xp)
				log.info("Gave ${player.name} $xp XP for ship-killing $killedName")
			}

			if (points > 0 && player.uniqueId != killed) player.rewardAchievement(Achievement.KILL_SHIP)
		}
	}

	private fun killMessage(killedName: String, data: ShipDamageData, arena: Boolean) {
		val descending = data.map.toList().sortedByDescending { it.second.get() }.toMutableList()
		val alertFeedbackColor = TextColor.color(Colors.ALERT)

		if (descending.isEmpty()) return

		// Begin killed ship formatting
		val killedShipName = data.name?.let {
			MiniMessage.miniMessage().deserialize(it)
		} ?: text("A ").color(alertFeedbackColor).append(data.type.component)

		val killedNationColor = SLPlayer.findIdByName(killedName)
			?.let { SLPlayer[it]?.nation?.let { nationID -> NationCache[nationID].color } }
			?: 16777215 // white // So many null checks, meh, it's not called too often.

		val killedShipHover = text()
			.append(text(data.size).color(NamedTextColor.WHITE))
			.append(text(" block ").color(NamedTextColor.WHITE))
			.append(data.type.component)
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
			.append(text(killedName).color(TextColor.color(killedNationColor)).hoverEvent(killedShipHover))
			.append(text(" was sunk by ").color(alertFeedbackColor))
			.append(killerMessage)

		if (data.map.size > 1) {
			message.append(text(", assisted by: ").color(alertFeedbackColor))

			var remainingAssists = data.map.size

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
				val killedShipDiscordName = data.name?.let { it.replace("<[^>]*>".toRegex(), "") + ", a" } ?: " a"
				val killerShipDiscordName = (killerShip as? ActiveControlledStarship)?.data?.name?.let {
					it.replace("<[^>]*>".toRegex(), "") + ", a"
				} ?: " a"

				val discordMessage =
					"$killedShipDiscordName ${data.size} block ${data.type.displayName}, piloted by $killedName, was shot down by " +
						if (killer.starship != null) { "${
							(killer.getDisplayName() as TextComponent).content()
						}, piloting $killerShipDiscordName ${killer.starship!!.initialBlockCount} block ${
							killerShip!!.type.displayName
						}."
						} else (killer.getDisplayName() as TextComponent).content()
				// end formatting

				val headURL = "https://minotar.net/avatar/$killedName"
				// end nice extras

				val embed = EmbedBuilder() // Build the embed
					.setTitle("Ship Kill") // Title at top
					.setTimestamp(Instant.now()) // Timestamp at the bottom
					.setColor(killedNationColor) // Color bar on the side is the killed player's nation's color
					.setThumbnail(headURL) // Head of the killed player
					.addField(MessageEmbed.Field(discordMessage, "", false))

				// Assists section
				if (data.map.size > 1) {
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
