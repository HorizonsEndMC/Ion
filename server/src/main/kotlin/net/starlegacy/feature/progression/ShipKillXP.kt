package net.starlegacy.feature.progression

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import github.scarsz.discordsrv.DiscordSRV
import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel
import net.horizonsend.ion.common.database.enums.Achievement
import net.horizonsend.ion.common.Colors
import net.horizonsend.ion.server.features.achievements.rewardAchievement
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import net.starlegacy.SLComponent
import net.starlegacy.database.schema.misc.SLPlayer
import net.starlegacy.database.schema.nations.Nation
import net.starlegacy.database.schema.nations.NationRelation
import net.starlegacy.feature.misc.CombatNPCKillEvent
import net.starlegacy.feature.starship.PilotedStarships.getDisplayNameComponent
import net.starlegacy.feature.starship.PilotedStarships.getRawDisplayName
import net.starlegacy.feature.starship.StarshipType
import net.starlegacy.feature.starship.active.ActivePlayerStarship
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.active.ActiveStarships
import net.starlegacy.feature.starship.event.StarshipExplodeEvent
import net.starlegacy.feature.starship.event.StarshipPilotedEvent
import net.starlegacy.util.Tasks
import org.bukkit.Bukkit
import org.bukkit.Bukkit.getOfflinePlayer
import org.bukkit.Bukkit.getPlayer
import org.bukkit.Bukkit.getServer
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.PlayerDeathEvent
import java.time.Instant
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.pow
import kotlin.math.sqrt

object ShipKillXP : SLComponent() {
	data class Damager(val id: UUID, val size: Int)

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
		val name = (starship as ActivePlayerStarship).data.name
		return ShipDamageData(map, size, type, name)
	}

	private val map: Cache<UUID, ShipDamageData> = CacheBuilder.newBuilder()
		.expireAfterWrite(5L, TimeUnit.MINUTES)
		.build()

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
		val arena = event.killer?.world?.name?.lowercase(Locale.getDefault())?.contains("arena") ?: true
		onPlayerKilled(event.id, event.name, event.killer, arena)
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	fun onPlayerDeath(event: PlayerDeathEvent) {
		val player: Player = event.entity
		val killer: Player? = player.killer
		val arena = event.player.world.name.lowercase(Locale.getDefault()).contains("arena")
		onPlayerKilled(player.uniqueId, player.name, killer, arena)
	}

	private fun addPassengers(starship: ActiveStarship) {
		for (id in starship.passengerIDs) {
			map.put(id, data(starship))
		}
	}

	private fun onPlayerKilled(killed: UUID, killedName: String, killer: Entity?, arena: Boolean) {
		val data = map.getIfPresent(killed) ?: return
		if (killer is Player) {
			val damager = Damager(killer.uniqueId, ActiveStarships.findByPassenger(killer)!!.initialBlockCount)
			data.map.getOrPut(damager) { AtomicInteger() }.incrementAndGet()
		}
		onShipKill(killed, killedName, data, arena)
	}

	private fun onShipKill(starship: ActiveStarship) {
		println("ship killed at ${starship.centerOfMass.x} ${starship.centerOfMass.y} ${starship.centerOfMass.z}")
		val data = data(starship)
		for (id in starship.passengerIDs) {
			val killedName = getPlayer(id)?.name ?: "UNKNOWN"
			val arena = starship.serverLevel.world.name.lowercase(Locale.getDefault()).contains("arena")
			onShipKill(id, killedName, data, arena)
		}
	}

	private fun onShipKill(killed: UUID, killedName: String, data: ShipDamageData, arena: Boolean) {
		val dataMap: Map<Damager, Int> = data.map.filterKeys { damager ->
			// require they be online to get xp
			// if they have this perm, e.g. someone in dutymode or on creative, they don't get xp
			getPlayer(damager.id)?.hasPermission("starships.noxp") == false
		}.mapValues { it.value.get() }

		val sum = dataMap.values.sum().toDouble()

		processDamagers(dataMap, data, sum, killedName, killed, arena)

		map.invalidate(killed)
	}

	private fun processDamagers(
		dataMap: Map<Damager, Int>,
		data: ShipDamageData,
		sum: Double,
		killedName: String,
		killed: UUID,
		arena: Boolean
	) {
		for ((damager, points) in dataMap.entries) {
			val player = getPlayer(damager.id) ?: continue // shouldn't happen
			val killedSize = data.size.toDouble()

			val pilotNation = SLPlayer[player].nation
			val killedNation = SLPlayer[getPlayer(killedName)!!].nation

			if (pilotNation != null && killedNation != null) {
				if (NationRelation.getRelationActual(pilotNation, killedNation).ordinal >= 5) {
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

			if (points > 0 && damager.id != killed) {
				getPlayer(damager.id)?.rewardAchievement(Achievement.KILL_SHIP)
			}
		}
		killMessage(killedName, data, arena)
	}

	private fun killMessage(killedName: String, data: ShipDamageData, arena: Boolean) {
		val descending = data.map.toList().sortedByDescending { it.second.get() }.toMutableList()
		val alertFeedbackColor = TextColor.color(Colors.ALERT)

		// Begin killed ship formatting
		val killedShipName = data.name?.let {
			MiniMessage.miniMessage().deserialize(it)
		} ?: Component.text("A ").color(alertFeedbackColor).append(data.type.component)

		val killedNationColor = SLPlayer.findIdByName(killedName)
			?.let { SLPlayer[it]?.nation?.let { nationID -> Nation.findById(nationID)?.color } }
			?: 16777215 // white // So many null checks, meh, it's not called too often.

		val killedShipHover = Component.text()
			.append(Component.text(data.size).color(NamedTextColor.WHITE))
			.append(Component.text(" block ").color(NamedTextColor.WHITE))
			.append(data.type.component)
			.build()
			.asHoverEvent()
		// End killed ship formatting

		// Begin killer ship formatting
		val killer = descending.first().first; descending.removeFirst() // remove to prevent duplicate during assists
		val killerShip = ActiveStarships.findByPassenger(getPlayer(killer.id)!!)!!

		val killerShipName =
			(killerShip as? ActivePlayerStarship)?.let { getDisplayNameComponent(it.data) }
				?: Component.text("a ").color(alertFeedbackColor).append(killerShip.type.component)

		val killerNationColor = SLPlayer[getPlayer(killer.id)!!].nation?.let {
				nationID ->
			Nation.findById(nationID)?.color
		} ?: 16777215 // white // So many null checks, meh, it's not called too often.

		val killerShipHover = Component.text()
			.append(Component.text(killerShip.initialBlockCount).color(NamedTextColor.WHITE))
			.append(Component.text(" block ").color(NamedTextColor.WHITE))
			.append(killerShip.type.component)
			.build()
			.asHoverEvent()
		// End killer ship formatting

		// Begin message
		val message = Component.text()

		if (arena) {
			message
				.append(Component.text("[").asComponent().color(TextColor.color(85, 85, 85)))
				.append(Component.text("Space Arena").asComponent().color(TextColor.color(255, 255, 102)))
				.append(Component.text("] ").asComponent().color(TextColor.color(85, 85, 85)))
		}

		message
			.append(killedShipName.hoverEvent(killedShipHover))
			.append(Component.text(" piloted by ").color(alertFeedbackColor).hoverEvent(killedShipHover))
			.append(Component.text(killedName).color(TextColor.color(killedNationColor)).hoverEvent(killedShipHover))
			.append(Component.text(" was sunk by ").color(alertFeedbackColor))
			.append(Component.text(getPlayer(killer.id)!!.name).color(TextColor.color(killerNationColor)).hoverEvent(killerShipHover))
			.append(Component.text(" piloting ").color(alertFeedbackColor).hoverEvent(killerShipHover))
			.append(killerShipName.hoverEvent(killerShipHover))

		if (data.map.size > 1) {
			message.append(Component.text(", assisted by: ").color(alertFeedbackColor))

			var remainingAssists = data.map.size

			for (assist in descending) {
				val assistPlayer = getPlayer(assist.first.id) ?: continue
				val assistShip = ActiveStarships.findByPilot(assistPlayer) ?: continue
				val assistNationColor = SLPlayer[assistPlayer].nation?.let {
						nationID ->
					Nation.findById(nationID)?.color
				} ?: 16777215 // white
				val assistShipName = (assistShip as? ActivePlayerStarship)?.let { getDisplayNameComponent(it.data) }
				val assistHoverEvent = Component.text()
					.append(Component.text(assistShip.initialBlockCount).color(NamedTextColor.WHITE))
					.append(Component.text(" block ").color(NamedTextColor.GRAY))
					.append(assistShip.type.component)
					.build()
					.asHoverEvent()

				message
					.append(Component.newline())
					.append(Component.text(assistPlayer.name).color(TextColor.color(assistNationColor)).hoverEvent(assistHoverEvent))
					.append(Component.text(" piloting ").color(alertFeedbackColor).hoverEvent(assistHoverEvent))

				assistShipName?.let {
					message
						.append(it)
						.append(Component.text(", a ").color(alertFeedbackColor))
				} ?: {
					message.append(Component.text("a ").color(alertFeedbackColor))
				}

				message
					.append(Component.text(assistShip.initialBlockCount).color(NamedTextColor.WHITE))
					.append(Component.text(" block ").color(alertFeedbackColor))
					.append(assistShip.type.component)

				remainingAssists--

				if (remainingAssists > 1) message.append(Component.text(",").color(alertFeedbackColor))
			}
		}

		getServer().sendMessage(message.build())

		if (Bukkit.getPluginManager().isPluginEnabled("DiscordSRV") && !arena) {
			Tasks.async {
				val channel: TextChannel = DiscordSRV.getPlugin()
					.getDestinationTextChannelForGameChannelName("events") ?: return@async

				// Formatting the messages
				val killedShipDiscordName = data.name?.let { it.replace("<[^>]*>".toRegex(), "") + ", a" } ?: " a"
				val killerShipDiscordName = (killerShip as? ActivePlayerStarship)?.data?.name?.let {
					it.replace("<[^>]*>".toRegex(), "") + ", a"
				} ?: " a"

				val discordMessage =
					"$killedShipDiscordName ${data.size} block ${data.type.displayName}, piloted by $killedName, was shot down by " +
						"${getOfflinePlayer(killer.id).name}, piloting $killerShipDiscordName ${killer.size} block ${killerShip.type.displayName}."
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

					for (assist in descending) {
						val assistPlayer = getPlayer(assist.first.id) ?: continue
						val assistShip = ActiveStarships.findByPilot(assistPlayer) ?: continue
						val assistName = assistShip.data.name?.let { getRawDisplayName(assistShip.data) + ", a" } ?: "a"

						assists += "${assistPlayer.name}, piloting $assistName ${assist.first.size} block ${assistShip.type.displayName}\n"
					}

					embed.addField(MessageEmbed.Field("Assisted by:", assists, false))
				}
				// End assists

				channel.sendMessageEmbeds(embed.build()).queue()
			}
		}
	}
}
