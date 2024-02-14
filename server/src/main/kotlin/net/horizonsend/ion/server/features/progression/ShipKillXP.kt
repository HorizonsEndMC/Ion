package net.horizonsend.ion.server.features.progression

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.player.CombatNPCKillEvent
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.damager.damager
import net.horizonsend.ion.server.features.starship.event.StarshipExplodeEvent
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.PlayerDeathEvent
import java.util.UUID
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

object ShipKillXP : IonServerComponent() {
	data class ShipDamageData(
		val points: AtomicInteger = AtomicInteger(),
		var lastDamaged: Long = System.currentTimeMillis()
	) {
		fun incrementPoints(by: Int): Int {
			lastDamaged = System.currentTimeMillis()
			return this.points.addAndGet(by)
		}
	}

	val damagerExpiration get() = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(5)

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	fun onStarshipExplode(event: StarshipExplodeEvent) {
		onShipKill(event.starship)
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	fun onCombatNPCKill(event: CombatNPCKillEvent) {
		onPlayerKilled(event.id, event.killer)
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	fun onPlayerDeath(event: PlayerDeathEvent) {
		val player: Player = event.entity
		val killer: Entity? = (player.lastDamageCause as? EntityDamageByEntityEvent)?.damager

		onPlayerKilled(player.uniqueId, killer)
	}

	private fun onPlayerKilled(killed: UUID, killer: Entity?) {
		val killedStarship = ActiveStarships.findByPilot(killed) ?: return

		killer?.let { killedStarship.addDamager(killer.damager(), 10_000) }

		onShipKill(killedStarship)
	}

	private fun onShipKill(starship: ActiveStarship) {
		log.info(
			"""
				ship "${starship.getDisplayNamePlain()}" killed at ${starship.centerOfMass}.
				Pilot: ${starship.controller}.
				Damagers: ${starship.damagers.entries.joinToString { "(Damager: ${it.key}, Points: ${it.value.points})" }}
				Rewards provider: ${starship.rewardsProviders.joinToString { it.javaClass.simpleName }}
				Sink message factory: ${starship.sinkMessageFactory}
			""".trimIndent()
		)

		starship.rewardsProviders.forEach { it.triggerReward() }
		starship.sinkMessageFactory.execute()
	}
}
