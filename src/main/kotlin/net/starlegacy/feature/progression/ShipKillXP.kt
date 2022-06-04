package net.starlegacy.feature.progression

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import net.horizonsend.ion.core.FeedbackType
import java.util.UUID
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.log2
import kotlin.math.sqrt
import net.starlegacy.SLComponent
import net.starlegacy.feature.misc.CombatNPCKillEvent
import net.starlegacy.feature.nations.region.Regions
import net.starlegacy.feature.nations.region.types.RegionCapturableStation
import net.starlegacy.feature.starship.StarshipType
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.active.ActiveStarships
import net.starlegacy.feature.starship.event.StarshipExplodeEvent
import net.starlegacy.feature.starship.event.StarshipPilotedEvent
import org.bukkit.Bukkit
import net.horizonsend.ion.core.sendFeedbackMessage
import org.bukkit.Bukkit.getPlayer
import org.bukkit.Bukkit.getServer
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.PlayerDeathEvent
import org.litote.kmongo.util.idValue

object ShipKillXP : SLComponent() {
	data class Damager(val id: UUID, val size: Int?)

	private data class ShipDamageData(
		val map: MutableMap<Damager, AtomicInteger>,
		val size: Int,
		val type: StarshipType
	)

	private fun data(starship: ActiveStarship): ShipDamageData {
		// needs to be a direct reference to the starship's damagers so it stays synchronized
		val map = starship.damagers
		val size = starship.blockCount
		val type = starship.type
		return ShipDamageData(map, size, type)
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
			val damager = Damager(killer.uniqueId, ActiveStarships.findByPassenger(killer)?.blockCount)
			data.map.getOrPut(damager, { AtomicInteger() }).incrementAndGet()
		}
		val isInStation = killer != null && isInStation(killer.location)
		onShipKill(killed, killedName, data, isInStation)
	}

	private fun onShipKill(starship: ActiveStarship) {
		val data = data(starship)
		val location = starship.centerOfMass.toLocation(starship.world)
		val isInStation = isInStation(location)
		for (id in starship.passengerIDs) {
			val killedName = Bukkit.getPlayer(id)?.name ?: "UNKNOWN"
			onShipKill(id, killedName, data, isInStation)
		}
	}

	private fun isInStation(location: Location): Boolean {
		return Regions.find(location).any { it is RegionCapturableStation }
	}

	private fun onShipKill(killed: UUID, killedName: String, data: ShipDamageData, isInStation: Boolean) {
		var baseXP = log2(data.size.toDouble()) * 200.0

		if (data.type.isWarship) {
			baseXP *= 2.5
		}

		if (isInStation) {
			baseXP *= 1.5
		}

		val dataMap: Map<Damager, Int> = data.map.filterKeys { damager ->
			// require they be online to get xp
			// if they have this perm, e.g. someone in dutymode or on creative, they don't get xp
			Bukkit.getPlayer(damager.id)?.hasPermission("starships.noxp") == false
		}.mapValues { it.value.get() }

		val sum = dataMap.values.sum().toDouble()

		processDamagers(dataMap, data, sum, baseXP, killedName)

		map.invalidate(killed)
	}

	private fun processDamagers(
		dataMap: Map<Damager, Int>,
		data: ShipDamageData,
		sum: Double,
		baseXP: Double,
		killedName: String
	) {
		for ((damager, points) in dataMap.entries) {
			val player = Bukkit.getPlayer(damager.id) ?: continue // shouldn't happen
			val killedSize = data.size.toDouble()
			val killerSize = damager.size?.toDouble() ?: killedSize // default to same size

			// xp is directly proportional to killed size and inversely proportional to killer size
			val sizeFactor = sqrt(killedSize) / sqrt(killerSize)

			val percent = points / sum
			val xp = (baseXP * sizeFactor * percent).toInt()
			if (xp > 0) {
				SLXP.addAsync(player, xp)
				log.info("Gave ${player.name} $xp XP for ship-killing $killedName")
				}
			var pointsrn = 0
			if (points > pointsrn) {
				pointsrn = points

				killMessage(killedName, damager, data)
			}
		}
	}

	private fun killMessage(killedName: String, damager: Damager, data: ShipDamageData) {
		val damagership = ActiveStarships.findByPassenger(getPlayer(damager.id)!!)!!.type.formatted
		getServer().sendFeedbackMessage(
			FeedbackType.ALERT,
			"<hover:show_text:'<gray>Block Count: {0}</gray>'>{1}</hover> piloted by {2} was sunk by {3} piloting <hover:show_text:'<gray>Block Count: {4}</gray>'>{5}</hover>\n",
			data.size,
			data.type.formatted,
			killedName,
			getPlayer(damager.id)!!.name,
			damager.size!!,
			damagership
		)
	}
}