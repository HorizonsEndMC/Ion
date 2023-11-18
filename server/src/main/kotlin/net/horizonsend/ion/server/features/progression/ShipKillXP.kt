package net.horizonsend.ion.server.features.progression

import net.horizonsend.ion.common.database.cache.nations.RelationCache
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.achievements.Achievement
import net.horizonsend.ion.server.features.achievements.rewardAchievement
import net.horizonsend.ion.server.features.misc.CombatNPCKillEvent
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.damager.PlayerDamager
import net.horizonsend.ion.server.features.starship.damager.PlayerDamagerWrapper
import net.horizonsend.ion.server.features.starship.event.StarshipExplodeEvent
import net.horizonsend.ion.server.miscellaneous.utils.get
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

object ShipKillXP : IonServerComponent() {
	data class ShipDamageData(
		val points: AtomicInteger = AtomicInteger(),
		var lastDamaged: Long = System.currentTimeMillis()
	)

	val damagerExpiration = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(5)

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	fun onStarshipExplode(event: StarshipExplodeEvent) {
		val arena = IonServer.configuration.serverName.equals("creative", ignoreCase = true)

		onShipKill(event.starship, event.starship.controller.pilotName.plainText(), arena)
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
			val damager = PlayerDamagerWrapper(killer)

			killedStarship.addToDamagers(damager)
		}

		onShipKill(killedStarship, killedName, arena)
	}

	private fun onShipKill(starship: ActiveStarship, killedPilotName: String, arena: Boolean) {
		log.info(
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

		processDamagers(starship, dataMap)
		(starship as? ActiveControlledStarship)?.sinkMessageFactory?.createAndSend()
	}

	private fun processDamagers(
		starship: ActiveStarship,
		dataMap: Map<Damager, ShipDamageData> // Filtered
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
				if (RelationCache[pilotNation, killedNation].ordinal >= 5) continue
			}

			val percent = points.get() / sum
			val xp = ((sqrt(killedSize.pow(2.0) / sqrt(killedSize * 0.00005))) * percent).toInt()

			if (xp > 0) {
				damager.rewardXP(xp)
				log.info("Gave ${player.name} $xp XP for ship-killing ${starship.controller.pilotName.plainText()}")
			}

			if (points.get() > 0 && player.uniqueId != killedPlayer?.uniqueId) player.rewardAchievement(Achievement.KILL_SHIP)
		}
	}
}
