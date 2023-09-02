package net.horizonsend.ion.server.features.starship.active

import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.miscellaneous.squared
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.starship.DeactivatedPlayerStarships
import net.horizonsend.ion.server.features.starship.PilotedStarships
import net.horizonsend.ion.server.features.starship.StarshipDestruction
import net.horizonsend.ion.server.features.starship.control.movement.PlayerStarshipControl.isHoldingController
import net.horizonsend.ion.server.features.starship.subsystem.weapon.StarshipWeapons
import net.horizonsend.ion.server.features.starship.subsystem.weapon.TurretWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.AutoWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.actionAndMsg
import org.bukkit.Bukkit
import org.bukkit.Bukkit.getPluginManager
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.dynmap.bukkit.DynmapPlugin
import java.util.LinkedList
import java.util.concurrent.TimeUnit
import kotlin.collections.component1
import kotlin.collections.component2

object ActiveStarshipMechanics : IonServerComponent() {
	override fun onEnable() {
		scheduleTasks()
	}

	private fun scheduleTasks() {
		Tasks.syncRepeat(20L, 20L, this::deactivateUnpilotedPlayerStarships)
		Tasks.syncRepeat(1L, 1L, this::chargeSubsystems)
		Tasks.syncRepeat(5L, 5L, this::fireAutoWeapons)
		Tasks.syncRepeat(60L, 60L, this::destroyLowHullIntegrityShips)
		Tasks.syncRepeat(20L, 20L, this::tickPlayers)
	}

	private fun deactivateUnpilotedPlayerStarships() {
		for (ship in ActiveStarships.allControlledStarships()) {
			val minutesUnpiloted = if (ship.controller != null) 0 else TimeUnit.NANOSECONDS.toMinutes(System.nanoTime() - ship.lastUnpilotTime)

			if (!PilotedStarships.isPiloted(ship) && minutesUnpiloted >= 10) {
				if (ship.controller == null && ship.minutesUnpiloted >= 5) {
					DeactivatedPlayerStarships.deactivateAsync(ship)
				}
			}
		}
	}

	private fun chargeSubsystems() {
		for (ship in ActiveStarships.all()) {
			val now = System.nanoTime()
			val delta: Double = TimeUnit.NANOSECONDS.toMillis(now - ship.lastTick).toDouble() / 1_000.0
			ship.lastTick = now

			ship.reactor.tick(delta)
		}
	}

	private fun fireAutoWeapons() {
		for (ship in ActiveStarships.all()) {
			val queuedShots = queueAutoShots(ship)
			StarshipWeapons.fireQueuedShots(queuedShots, ship)
		}
	}

	private fun queueAutoShots(ship: ActiveStarship): LinkedList<StarshipWeapons.AutoQueuedShot> {
		val queuedShots = LinkedList<StarshipWeapons.AutoQueuedShot>()

		for ((node, target) in ship.autoTurretTargets) {
			val targetLocation = target.type.get(target.identifier) ?: continue
			if (targetLocation.world != ship.world) continue

			val weapons = ship.weaponSets[node]

			for (weapon in weapons) {
				if (weapon !is AutoWeaponSubsystem) continue
				if (!weapon.isIntact()) continue

				val targetVec = targetLocation.toVector()
				val direct = targetVec.clone().subtract(ship.centerOfMass.toCenterVector()).normalize()

				if (targetVec.distanceSquared(weapon.pos.toCenterVector()) > weapon.range.squared()) continue

				val dir = weapon.getAdjustedDir(direct, targetVec)

				if (weapon is TurretWeaponSubsystem && !weapon.ensureOriented(dir)) continue
				if (!weapon.isCooledDown()) continue
				if (!weapon.canFire(dir, targetVec)) continue

				queuedShots.add(StarshipWeapons.AutoQueuedShot(weapon, target, dir))
			}
		}

		return queuedShots
	}

	private fun destroyLowHullIntegrityShips() {
		ActiveStarships.all().forEach { ship ->
			if (ship.hullIntegrity() < 0.8) {
				StarshipDestruction.destroy(ship)
			}
		}
	}

	@EventHandler
	fun onBlockBreak(event: BlockBreakEvent) {
		if (ActiveStarships.findByBlock(event.block) != null) {
			event.isCancelled = true
			event.player actionAndMsg "&cThat block is part of an active starship!"
		}
	}

	@EventHandler
	fun onPlayerMove(event: PlayerMoveEvent) {
		val player = event.player
		val starship = ActiveStarships.findByPassenger(player) ?: return
		val (x, y, z) = Vec3i(event.to)
		if (starship.isWithinHitbox(x, y, z, 2)) {
			return
		}
		event.isCancelled = true
		player.userError("Can't leave piloted ship. To leave, use /stopriding.")
		// a tick later
		Tasks.sync {
			if (!starship.isWithinHitbox(player)) {
				if (PilotedStarships[player] == starship) {
					PilotedStarships.unpilot(starship)
					player.userError("You got outside of the ship, so it was unpiloted!")
				} else {
					starship.removePassenger(player.uniqueId)
					player.userError("You got outside of the ship, so you're no longer riding it!")
				}
			}
		}
	}

	private var allowBlockExplosion = false

	fun withBlockExplosionDamageAllowed(block: () -> Unit) {
		try {
			allowBlockExplosion = true
			block()
		} finally {
			allowBlockExplosion = false
		}
	}

	@EventHandler
	fun onEntityDamage(event: EntityDamageEvent) {
		// block explosion damage has too many problems with starships
		if (event.cause != EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) {
			return
		}

		if (allowBlockExplosion) {
			return
		}

		event.isCancelled = true
	}

	private fun tickPlayers() {
		for (player in IonServer.server.onlinePlayers) {
			val starship = ActiveStarships.findByPilot(player)
			updateDynmapVisibility(player, starship)
			updateGlowing(player, starship)
		}
	}

	private fun updateDynmapVisibility(player: Player, starship: ActiveControlledStarship?) {
		if (!getPluginManager().isPluginEnabled("dynmap")) return

		val isNoStarship = starship == null
		val isHoldingController = isHoldingController(player)
		val isInvisible = isNoStarship && !isHoldingController
		DynmapPlugin.plugin.assertPlayerInvisibility(player, isInvisible, IonServer)
	}

	private fun updateGlowing(player: Player, starship: ActiveControlledStarship?) {
		val shouldGlow = starship != null
		if (player.isGlowing != shouldGlow) {
			player.isGlowing = shouldGlow
		}
	}

	override fun onDisable() {
		// release all ships on shutdown
		ActiveStarships.allControlledStarships().forEach { DeactivatedPlayerStarships.deactivateNow(it) }
	}
}
