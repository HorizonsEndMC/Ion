package net.starlegacy.feature.progression

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import github.scarsz.discordsrv.DiscordSRV
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel
import net.horizonsend.ion.common.database.collections.PlayerData
import net.horizonsend.ion.common.database.update
import net.horizonsend.ion.server.legacy.events.ShipKillEvent
import net.horizonsend.ion.server.legacy.feedback.FeedbackType
import net.horizonsend.ion.server.legacy.feedback.sendFeedbackMessage
import net.starlegacy.SLComponent
import net.starlegacy.database.schema.misc.SLPlayer
import net.starlegacy.database.schema.nations.NationRelation
import net.starlegacy.feature.misc.CombatNPCKillEvent
import net.starlegacy.feature.starship.PilotedStarships.getDisplayName
import net.starlegacy.feature.starship.StarshipType
import net.starlegacy.feature.starship.active.ActivePlayerStarship
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.active.ActiveStarships
import net.starlegacy.feature.starship.event.StarshipExplodeEvent
import net.starlegacy.feature.starship.event.StarshipPilotedEvent
import net.starlegacy.util.Tasks
import org.bukkit.Bukkit
import org.bukkit.Bukkit.getPlayer
import org.bukkit.Bukkit.getServer
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.PlayerDeathEvent
import java.util.UUID
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.pow
import kotlin.math.sqrt

object ShipKillXP : SLComponent() {
	data class Damager(val id: UUID, val size: Int?)

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
		onPlayerKilled(event.id, event.name, event.killer)
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	fun onPlayerDeath(event: PlayerDeathEvent) {
		val player: Player = event.entity
		val killer: Player? = player.killer
		onPlayerKilled(player.uniqueId, player.name, killer)
	}

	private fun addPassengers(starship: ActiveStarship) {
		for (id in starship.passengerIDs) {
			map.put(id, data(starship))
		}
	}

	private fun onPlayerKilled(killed: UUID, killedName: String, killer: Entity?) {
		val data = map.getIfPresent(killed) ?: return
		if (killer is Player) {
			val damager = Damager(killer.uniqueId, ActiveStarships.findByPassenger(killer)?.initialBlockCount)
			data.map.getOrPut(damager) { AtomicInteger() }.incrementAndGet()
		}
		onShipKill(killed, killedName, data)
	}

	private fun onShipKill(starship: ActiveStarship) {
		val data = data(starship)
		for (id in starship.passengerIDs) {
			val killedName = getPlayer(id)?.name ?: "UNKNOWN"
			onShipKill(id, killedName, data)
		}
	}

	private fun onShipKill(killed: UUID, killedName: String, data: ShipDamageData) {
		val dataMap: Map<Damager, Int> = data.map.filterKeys { damager ->
			// require they be online to get xp
			// if they have this perm, e.g. someone in dutymode or on creative, they don't get xp
			getPlayer(damager.id)?.hasPermission("starships.noxp") == false
		}.mapValues { it.value.get() }

		val sum = dataMap.values.sum().toDouble()

		processDamagers(dataMap, data, sum, killedName, killed)

		map.invalidate(killed)
	}

	private fun processDamagers(
		dataMap: Map<Damager, Int>,
		data: ShipDamageData,
		sum: Double,
		killedName: String,
		killed: UUID
	) {
		for ((damager, points) in dataMap.entries) {
			val player = getPlayer(damager.id) ?: continue // shouldn't happen
			val killedSize = data.size.toDouble()

			val pilotNation = SLPlayer[player].nation
			val killedNation = SLPlayer[getPlayer(killedName)!!].nation

			if (pilotNation != null && killedNation != null) {
				if (NationRelation.getRelationActual(pilotNation, killedNation).ordinal >= 5) return
			}

			val percent = points / sum
			val xp = ((sqrt(killedSize.pow(2.0) / sqrt(killedSize * 0.00005))) * percent).toInt()

			if (xp > 0) {
				SLXP.addAsync(player, xp)
				log.info("Gave ${player.name} $xp XP for ship-killing $killedName")
			}

			if (points > 0) {
				killMessage(killedName, damager, data)
				ShipKillEvent(getPlayer(killed)!!, getPlayer(damager.id)!!).callEvent()
				PlayerData[damager.id].update {
					bounty += xp
				}
			}
		}
	}

	private fun killMessage(killedName: String, damager: Damager, data: ShipDamageData) {
		val damagerShip = ActiveStarships.findByPassenger(getPlayer(damager.id)!!)!!
		val damagerShipName =
			(damagerShip as? ActivePlayerStarship)?.let { getDisplayName(damagerShip.data) } ?: damagerShip.type.formatted

		getServer().sendFeedbackMessage(
			FeedbackType.ALERT,
			"<hover:show_text:'<gray>Block Count: {0}</gray>'>{1}</hover> piloted by {2} was sunk by {3} piloting <hover:show_text:'<gray>Block Count: {4}</gray>'>{5}</hover>\n",
			data.size,
			data.type.formatted,
			killedName,
			getPlayer(damager.id)!!.name,
			damager.size!!,
			damagerShipName
		)

		if (Bukkit.getPluginManager().isPluginEnabled("DiscordSRV")) {
			Tasks.async {
				val channel: TextChannel? = DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName("events")

				if (channel == null) {
					System.err.println("ERROR: No events channel found!")
					return@async
				}

				val newShipKillDiscordMessage =
					"${data.name}, a ${data.size} block ${data.type}, piloted by $killedName, was shot down by " +
						"${getPlayer(damager.id)!!.name}, piloting $damagerShipName, a ${damager.size} block ${damagerShip.type}."

				channel.sendMessage(newShipKillDiscordMessage).queue()
			}
		}
	}
}
