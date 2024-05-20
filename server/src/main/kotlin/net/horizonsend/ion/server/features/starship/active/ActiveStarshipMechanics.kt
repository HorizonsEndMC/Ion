package net.horizonsend.ion.server.features.starship.active

import net.horizonsend.ion.common.extensions.alert
import net.horizonsend.ion.common.extensions.userErrorAction
import net.horizonsend.ion.common.utils.miscellaneous.squared
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.starship.DeactivatedPlayerStarships
import net.horizonsend.ion.server.features.starship.PilotedStarships
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.control.controllers.player.ActivePlayerController
import net.horizonsend.ion.server.features.starship.control.movement.PlayerStarshipControl.isHoldingController
import net.horizonsend.ion.server.features.starship.damager.addToDamagers
import net.horizonsend.ion.server.features.starship.damager.entityDamagerCache
import net.horizonsend.ion.server.features.starship.destruction.StarshipDestruction
import net.horizonsend.ion.server.features.starship.destruction.StarshipDestruction.MAX_SAFE_HULL_INTEGRITY
import net.horizonsend.ion.server.features.starship.event.StarshipUnpilotedEvent
import net.horizonsend.ion.server.features.starship.subsystem.SupercapReactorSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.StarshipWeapons
import net.horizonsend.ion.server.features.starship.subsystem.weapon.TurretWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.AutoWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.Bukkit.getPluginManager
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.entity.TNTPrimed
import org.bukkit.entity.minecart.ExplosiveMinecart
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityExplodeEvent
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
		Tasks.syncRepeat(60L, 60L, this::handleBattlecruiserMechanics)
		Tasks.syncRepeat(20L, 20L, this::tickPlayers)
	}

	private fun deactivateUnpilotedPlayerStarships() {
		for (ship in ActiveStarships.allControlledStarships()) {
			if (ship.minutesUnpiloted >= 5) DeactivatedPlayerStarships.deactivateAsync(ship)
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
			ship.updateHullIntegrity()
			if (ship.hullIntegrity < MAX_SAFE_HULL_INTEGRITY) {
				ship.alert("Critical hull integrity failure!")

				StarshipDestruction.destroy(ship)
			}
		}
	}

	const val BATTLECRUISER_FUEL_CONSUMPTION = 18

	private fun handleBattlecruiserMechanics() {
		// Consume fuel
		ActiveStarships.all()
			.filter { it.type == StarshipType.BATTLECRUISER || it.type == StarshipType.CRUISER }
			.filter { it.controller is ActivePlayerController }
			.forEach { battlecruiser: ActiveStarship ->

			var remaining = BATTLECRUISER_FUEL_CONSUMPTION

			for (fuelTank in battlecruiser.fuelTanks) {
				remaining -= fuelTank.tryConsumeFuel(remaining)

				if (remaining <= 0) break
			}

			if (remaining <= 0) return@forEach

			battlecruiser.alert("WARNING: Fuel depleted! Shutdown sequence initiated")
			PilotedStarships.unpilot(battlecruiser as ActiveControlledStarship)
		}

		// Destroy without intact reactors
		ActiveStarships.all().filter { it.type == StarshipType.BATTLECRUISER }.forEach { ship ->
			if (ship.subsystems.filterIsInstance<SupercapReactorSubsystem>().none { it.isIntact() }) {
				ship.alert("All reactors are down, ship explosion imminent!")
				StarshipDestruction.destroy(ship)
			}
		}
	}

	@EventHandler
	fun onEntityExplode(event: EntityExplodeEvent) {
		val block = event.location.block
		val world = block.world

		val entity: Entity = when (val entity = event.entity) {
			is Projectile -> entity.shooter as? Entity
			is TNTPrimed -> entity.source
			is ExplosiveMinecart -> return
			else -> entity
		} ?: return

		val damager = entityDamagerCache[entity]

		addToDamagers(world, block, damager)
	}

	@EventHandler
	fun onBlockBreak(event: BlockBreakEvent) {
		if (ActiveStarships.findByBlock(event.block) != null) {
			event.isCancelled = true
			event.player.userErrorAction("That block is part of an active starship!")
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
		player.userErrorAction("Can't leave piloted ship. To leave, use /stopriding.")
		// a tick later
		Tasks.sync {
			if (!starship.isWithinHitbox(player)) {
				if (PilotedStarships[player] == starship) {
					PilotedStarships.unpilot(starship)
					player.userErrorAction("You got outside of the ship, so it was unpiloted!")
				} else {
					starship.removePassenger(player.uniqueId)
					player.userErrorAction("You got outside of the ship, so you're no longer riding it!")
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

	@EventHandler
	fun onAIUnpilot(event: StarshipUnpilotedEvent) {
		val starship = event.starship

		if (starship.controller !is AIController && !starship.isExploding) return

		StarshipDestruction.vanish(starship)
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
