package net.horizonsend.ion.server.features.starship.active

import net.horizonsend.ion.common.extensions.alert
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.userErrorAction
import net.horizonsend.ion.common.utils.miscellaneous.squared
import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.configuration.ServerConfiguration
import net.horizonsend.ion.server.core.IonServerComponent
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.player.CombatTimer
import net.horizonsend.ion.server.features.sidebar.tasks.ContactsSidebar
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.starship.DeactivatedPlayerStarships
import net.horizonsend.ion.server.features.starship.PilotedStarships
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.control.controllers.player.ActivePlayerController
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.damager.addToDamagers
import net.horizonsend.ion.server.features.starship.damager.entityDamagerCache
import net.horizonsend.ion.server.features.starship.destruction.StarshipDestruction
import net.horizonsend.ion.server.features.starship.destruction.StarshipDestruction.MAX_SAFE_HULL_INTEGRITY
import net.horizonsend.ion.server.features.starship.event.StarshipUnpilotEvent
import net.horizonsend.ion.server.features.starship.status_effects.StarshipStatusEffect
import net.horizonsend.ion.server.features.starship.status_effects.StarshipStatusEffectType
import net.horizonsend.ion.server.features.starship.status_effects.StarshipStatusEffectTypes
import net.horizonsend.ion.server.features.starship.subsystem.checklist.BargeReactorSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.checklist.BattlecruiserReactorSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.checklist.CruiserReactorSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.checklist.FauxReactorSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.checklist.LargeReactorSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.checklist.MediumReactorSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.BalancedWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.StarshipWeapons.AutoQueuedShot
import net.horizonsend.ion.server.features.starship.subsystem.weapon.StarshipWeapons.fireQueuedShots
import net.horizonsend.ion.server.features.starship.subsystem.weapon.TurretWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.AutoWeaponSubsystem
import net.horizonsend.ion.server.features.world.IonWorld.Companion.hasFlag
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.features.world.WorldFlag
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.distanceSquared
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.spherePoints
import net.horizonsend.ion.server.miscellaneous.utils.enumSetOf
import org.bukkit.Bukkit.getPluginManager
import org.bukkit.Color
import org.bukkit.Particle
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
import java.time.Duration
import java.util.LinkedList
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt
import kotlin.math.sin


object ActiveStarshipMechanics : IonServerComponent() {
	override fun onEnable() {
		scheduleTasks()
	}

	private fun scheduleTasks() {
		Tasks.syncRepeat(20L, 20L, this::deactivateUnpilotedPlayerStarships)
		Tasks.syncRepeat(1L, 1L, this::chargeSubsystems)
		Tasks.syncRepeat(5L, 5L, this::fireAutoWeapons)
		Tasks.syncRepeat(60L, 60L, this::destroyLowHullIntegrityShips)
		Tasks.syncRepeat(60L, 60L, this::handleSupercapitalMechanics)
		Tasks.syncRepeat(20L, 20L, this::tickPlayers)
		Tasks.syncRepeat(20L, 20L, this::updateStarshipStatusEffects)
		Tasks.syncRepeat(5L, 5L, this::displayJumpBeaconEffect)
		Tasks.syncRepeat(4L, 4L, this::refreshDisruptorStatusEffects)
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
			if (ship.world.hasFlag(WorldFlag.PLANET_SIEGE_WORLD)) return
			val queuedShots = queueAutoShots(ship)
			fireQueuedShots(queuedShots, ship)
		}
	}

	private fun queueAutoShots(ship: ActiveStarship): LinkedList<AutoQueuedShot> {
		val queuedShots = LinkedList<AutoQueuedShot>()

		for ((node, target) in ship.autoTurretTargets) {
			val targetLocation = target.location(ship) ?: continue
			if (targetLocation.world != ship.world) continue

			val weapons = ship.weaponSets[node]

			for (weapon in weapons) {
				if (weapon !is AutoWeaponSubsystem) continue
				if (!weapon.isIntact()) continue

				val targetVec = targetLocation.toVector()
				val direct = targetVec.clone().subtract(ship.centerOfMass.toCenterVector()).normalize()

				if (targetVec.distanceSquared(weapon.pos.toCenterVector()) > weapon.range.squared()) continue

				val dir = weapon.getAdjustedDir(direct, targetVec)

				if (weapon is TurretWeaponSubsystem<*, *> && !weapon.ensureOriented(dir)) continue
				if (weapon is BalancedWeaponSubsystem<*> && !weapon.isCooledDown()) continue
				if (!weapon.canFire(dir, targetVec)) continue

				queuedShots.add(AutoQueuedShot(weapon, target, dir))
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

	const val SUPERCAPITAL_FUEL_CONSUMPTION = 9

	private fun handleSupercapitalMechanics() {
		// Consume fuel
		ActiveStarships.all()
			.filter { it.type.needsFuel }
			//TODO replace this system with something better
			.filter { it.controller is ActivePlayerController }
			.filter { !it.world.ion.hasFlag(WorldFlag.NO_SUPERCAPITAL_REQUIREMENTS) } // consume fuel if world did not disable supercapital requirements
			.forEach { superCapital: ActiveStarship ->

			var remaining = SUPERCAPITAL_FUEL_CONSUMPTION

			for (fuelTank in superCapital.fuelTanks) {
				remaining -= fuelTank.tryConsumeFuel(remaining)

				if (remaining <= 0) break
			}

			if (remaining <= 0) return@forEach

			superCapital.alert("WARNING: Fuel depleted! Shutdown sequence initiated")
			PilotedStarships.unpilot(superCapital)
		}

		//TODO replace this system with something better

		// Destroy BCs without intact reactors
		ActiveStarships.all().filter { it.type == StarshipType.BATTLECRUISER && !it.world.ion.hasFlag(WorldFlag.NO_SUPERCAPITAL_REQUIREMENTS) }.forEach { ship ->
			if (ship.subsystems.filterIsInstance<BattlecruiserReactorSubsystem>().none { it.isIntact() }) {
				ship.alert("All reactors are down, ship explosion imminent!")
				StarshipDestruction.destroy(ship)
			}
		}

		// Destroy Cruisers without intact reactors
		ActiveStarships.all().filter { it.type == StarshipType.CRUISER && !it.world.ion.hasFlag(WorldFlag.NO_SUPERCAPITAL_REQUIREMENTS) }.forEach { ship ->
			if (ship.subsystems.filterIsInstance<CruiserReactorSubsystem>().none { it.isIntact() }) {
				ship.alert("All reactors are down, ship explosion imminent!")
				StarshipDestruction.destroy(ship)
			}
		}

		// Destroy large tech 2 ships without intact reactors
		ActiveStarships.all().filter {
			(it.type == StarshipType.MISSILE_CRUISER ||
			it.type == StarshipType.LOGISTICS_CRUISER ||
			it.type == StarshipType.LANCER_BATTLECRUISER) &&
			!it.world.ion.hasFlag(WorldFlag.NO_SUPERCAPITAL_REQUIREMENTS) }.forEach { ship ->
			if (ship.subsystems.filterIsInstance<LargeReactorSubsystem>().none { it.isIntact() }) {
				ship.alert("All reactors are down, ship explosion imminent!")
				StarshipDestruction.destroy(ship)
			}
		}

		// Destroy medium tech 2 ships without intact reactors
		ActiveStarships.all().filter {
			(it.type == StarshipType.ASSAULT_FRIGATE ||
			it.type == StarshipType.BLACK_OPS_FRIGATE ||
			it.type == StarshipType.MISSILE_FRIGATE ||
			it.type == StarshipType.ASSAULT_DESTROYER ||
			it.type == StarshipType.INTERDICTOR_DESTROYER) &&
			!it.world.ion.hasFlag(WorldFlag.NO_SUPERCAPITAL_REQUIREMENTS) }.forEach { ship ->
			if (ship.subsystems.filterIsInstance<MediumReactorSubsystem>().none { it.isIntact() }) {
				ship.alert("All reactors are down, ship explosion imminent!")
				StarshipDestruction.destroy(ship)
			}
		}

		ActiveStarships.all().filter { it.type == StarshipType.BARGE && !it.world.ion.hasFlag(WorldFlag.NO_SUPERCAPITAL_REQUIREMENTS) }.forEach { ship ->
			if (ship.subsystems.filterIsInstance<BargeReactorSubsystem>().none { it.isIntact() }) {
				ship.alert("All reactors are down, ship explosion imminent!")
				StarshipDestruction.destroy(ship)
			}
		}

		ActiveStarships.all().filter { it.type in enumSetOf(StarshipType.AI_BARGE, StarshipType.AI_BATTLECRUISER, StarshipType.AI_CRUISER) && !it.world.ion.hasFlag(WorldFlag.NO_SUPERCAPITAL_REQUIREMENTS) }.forEach { ship ->
			if (ship.subsystems.filterIsInstance<FauxReactorSubsystem>().none { it.isIntact() }) {
				ship.alert("All reactors are down, ship explosion imminent!")
				StarshipDestruction.destroy(ship)
			}
		}
	}

	private fun updateStarshipStatusEffects() {
		ActiveStarships.all().forEach { starship ->
			val statusEffects = starship.statusEffects

			// Find the status effect with the largest strength value, and set it to the active effect
			statusEffects.mapValues { (_, statusEffectList) -> statusEffectList.forEach { statusEffect -> statusEffect.isActive = false } }
			statusEffects.mapValues { (_, statusEffectList) ->
				val highestStrengthEffect = statusEffectList.maxByOrNull { statusEffect -> statusEffect.strength } ?: return@mapValues
				highestStrengthEffect.isActive = true
			}

			statusEffects.mapValues { (_, statusEffectList) -> statusEffectList.forEach { statusEffect -> statusEffect.durationMillis -= TimeUnit.SECONDS.toMillis(1) } }
			statusEffects.mapValues { (_, statusEffectList) -> statusEffectList.removeAll { statusEffect ->
				if (statusEffect.durationMillis <= 0) {
					if (statusEffect.type.displayType == StarshipStatusEffectType.DisplayType.PERCENT) {
						starship.information("Status effect ${statusEffect.type.displayName.plainText()} with strength ${(statusEffect.strength * 100).roundToInt()}% has worn off")
					} else {
						starship.information("Status effect ${statusEffect.type.displayName.plainText()} with strength ${(statusEffect.strength).roundToInt()} has worn off")
					}
				}
				statusEffect.durationMillis <= 0
			} }
		}
	}

	var jumpBeaconTick = 0
	private fun displayJumpBeaconEffect() {
		val particleData = Particle.DustTransition(
			Color.BLUE,
			Color.ORANGE,
			4f
		)

		ActiveStarships.all().filter { starship -> starship.isJumpBeaconOn }.forEach { starship ->
			val com = starship.centerOfMass.toLocation(starship.world).toCenterLocation()
			val points = com.spherePoints(2.5 * sin(jumpBeaconTick / (2 * Math.PI)) + 7.5, (20 * sin(jumpBeaconTick / (2 * Math.PI)) + 60).toInt())
			for (point in points) {
				val toCenter = com.toVector().subtract(point.toVector()).normalize().multiply(5)

				starship.world.spawnParticle(
					Particle.DUST_COLOR_TRANSITION,
					point.x,
					point.y,
					point.z,
					0,
					toCenter.x,
					toCenter.y,
					toCenter.z,
					2.0,
					particleData,
					true
				)
			}
		}

		jumpBeaconTick += 1
	}

	private fun refreshDisruptorStatusEffects() {
		for (starship in ActiveStarships.all().filter { starship -> starship.disruptorTarget != null }) {
			// disable disrupt if the target is no longer active
			val disruptorTarget = starship.disruptorTarget
			if (disruptorTarget == null || ActiveStarships.getByIdentifier(disruptorTarget.identifier) == null) {
				starship.information("Your disrupted target is no longer active!")
				starship.setIsDisrupting(null)
				continue
			}

			// only refresh disrupt in the same world + in space (definitely not hyperspace)
			if (starship.world != disruptorTarget.world) continue
			if (!starship.world.hasFlag(WorldFlag.SPACE_WORLD)) continue

			// do not refresh if out of range
			if (starship.centerOfMass.distanceSquared(disruptorTarget.centerOfMass)
				> disruptorTarget.balancing.interdictionRange * disruptorTarget.balancing.interdictionRange) continue

			disruptorTarget.addStatusEffect(
				StarshipStatusEffect(
					StarshipStatusEffectTypes.WARP_DISRUPTED,
					starship.type.balancing.wellStrength,
					Duration.ofSeconds(5L).toMillis(),
					starship
				)
			)

			val disruptingController = starship.controller
			val disruptedController = disruptorTarget.controller

			if (disruptingController is PlayerController && disruptedController is PlayerController) {
				CombatTimer.evaluateSvs(disruptingController.damager, disruptorTarget)
			} else if ((disruptingController is AIController && disruptedController is PlayerController) || (disruptingController is PlayerController && disruptedController is AIController)) {
				CombatTimer.evaluateSvs(disruptingController.damager, disruptorTarget)
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

		addToDamagers(world, block, damager) { starship ->
			starship.lastWeaponName = entity.name()
		}
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
	fun onAIUnpilot(event: StarshipUnpilotEvent) {
		val starship = event.starship

		if (event.oldController !is AIController) return
		if (starship.isExploding) return

		StarshipDestruction.vanish(starship, urgent = true)
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

		val poiCheck = isInPOI(player, starship)

		val shouldBeVisible = isInSuperPOI(player, starship)

		val isInvisible = !poiCheck && !shouldBeVisible && !(starship?.isJumpBeaconOn ?: false)
		DynmapPlugin.plugin.assertPlayerInvisibility(player, isInvisible, IonServer)
	}

	/**
	 * Map visibility check
	 */
	private fun isInPOI(player: Player, starship: ActiveControlledStarship?): Boolean {
		if (starship?.type == StarshipType.RECON_STARFIGHTER) return false

		val beacons = ConfigurationFiles.serverConfiguration().beacons
			.filter { it.spaceLocation.world == player.world.name }

		// not visible if not in a core region
		if (starship?.world?.hasFlag(WorldFlag.CORE_REGION_WORLD) == false) return false

		if (starship != null) {
			for (beacon in beacons) if (beacon.proximityReveal == true && (distanceSquared(beacon.spaceLocation.toVec3i(), starship.centerOfMass) <= 500 * 500)) return true
		} else {
			for (beacon in beacons) if (beacon.proximityReveal == true && (distanceSquared(beacon.spaceLocation.toVector(), player.location.toVector()) <= 500 * 500)) return true
		}
		return false
	}

	/**
	 * Second, important visibility check (basically only for combat)
	 */
	private fun isInSuperPOI(player: Player, starship: ActiveControlledStarship?): Boolean {
		if (starship?.type == StarshipType.RECON_STARFIGHTER) return false
		return CombatTimer.isPvpCombatTagged(player)
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
