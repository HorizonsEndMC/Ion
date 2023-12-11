package net.horizonsend.ion.server.features.progression

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.misc.CombatNPCKillEvent
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.damager.PlayerDamagerWrapper
import net.horizonsend.ion.server.features.starship.event.StarshipExplodeEvent
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.PlayerDeathEvent
import java.util.UUID
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

object ShipKillXP : IonServerComponent() {
	data class ShipDamageData(
		val points: AtomicInteger = AtomicInteger(),
		var lastDamaged: Long = System.currentTimeMillis()
	)

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
		val killer: Player? = player.killer
		val arena = IonServer.configuration.serverName.equals("creative", ignoreCase = true)

		onPlayerKilled(player.uniqueId, killer)
	}

	private fun onPlayerKilled(killed: UUID, killer: Entity?) {
		val killedStarship = ActiveStarships.findByPilot(killed) ?: return

		if (killer is Player) {
			val damager = PlayerDamagerWrapper(killer)

			killedStarship.addToDamagers(damager)
		}

		onShipKill(killedStarship)
	}

	private fun onShipKill(starship: ActiveStarship) {
		log.info(
			"""
				ship killed at ${starship.centerOfMass}.
				Pilot: ${starship.controller}.
				Damagers: ${starship.damagers.entries.joinToString { "(Damager: ${it.key}, Points: ${it.value.points})" }}
			""".trimIndent()
		)

		starship.rewardsProvider.onSink()
		starship.sinkMessageFactory.execute()
	}
}
