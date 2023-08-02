package net.horizonsend.ion.server.features.starship.hyperspace

import net.horizonsend.ion.server.features.achievements.Achievement
import net.horizonsend.ion.common.extensions.serverError
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.extensions.userErrorAction
import net.horizonsend.ion.server.features.achievements.rewardAchievement
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.space.SpaceWorlds
import net.horizonsend.ion.server.features.starship.StarshipType.PLATFORM
import net.horizonsend.ion.server.features.starship.active.ActivePlayerStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.event.StarshipActivatedEvent
import net.horizonsend.ion.server.features.starship.event.StarshipDeactivatedEvent
import net.horizonsend.ion.server.features.starship.event.StarshipMoveEvent
import net.horizonsend.ion.server.features.starship.event.StarshipRotateEvent
import net.horizonsend.ion.server.features.starship.event.StarshipTranslateEvent
import net.horizonsend.ion.server.features.starship.movement.StarshipTeleportation
import net.horizonsend.ion.server.features.starship.subsystem.HyperdriveSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.NavCompSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.toLocation
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.event.EventHandler
import kotlin.math.log10
import kotlin.math.sqrt

object Hyperspace : IonServerComponent() {
	private val warmupTasks = mutableMapOf<ActiveStarship, HyperspaceWarmup>()
	private val movementTasks = mutableMapOf<ActiveStarship, HyperspaceMovement>()

	fun isWarmingUp(starship: ActiveStarship) = warmupTasks.containsKey(starship)
	fun isMoving(starship: ActiveStarship) = movementTasks.containsKey(starship)

	const val HYPERMATTER_AMOUNT = 2
	const val INTER_SYSTEM_DISTANCE = 60000

	override fun onDisable() {
		movementTasks.forEach { (_, hyperspaceMovement) ->
			cancelJumpMovement(hyperspaceMovement)
		}
	}

	fun beginJumpWarmup(
		starship: ActiveStarship,
		hyperdrive: HyperdriveSubsystem,
		x: Int,
		z: Int,
		destinationWorld: World,
		useFuel: Boolean
	) {
		if (MassShadows.find(
				starship.serverLevel.world,
				starship.centerOfMass.x.toDouble(),
				starship.centerOfMass.z.toDouble()
			) != null
		) {
			starship.userError("Ship is within Gravity Well, jump cancelled")
			return
		}
		if (starship.type == PLATFORM) {
			starship.onlinePassengers.forEach {
				it.userErrorAction("This ship type is not capable of moving.")
			}
			return
		}

		check(!isWarmingUp(starship)) { "Starship is already warming up!" }
		check(!isMoving(starship)) { "Starship is already moving in hyperspace" }
		check(hyperdrive.isIntact()) { "Hyperdrive @ ${hyperdrive.pos} damaged" }

		val spaceWorld = starship.serverLevel.world
		check(SpaceWorlds.contains(spaceWorld)) { "${spaceWorld.name} is not a space world" }

		val hyperspaceWorld = getHyperspaceWorld(spaceWorld)
		checkNotNull(hyperspaceWorld) { "${spaceWorld.name} does not have a hyperspace world" }

		val dest = Location(destinationWorld, x.toDouble(), 128.0, z.toDouble())
		val mass = starship.mass
		val speed = calculateSpeed(hyperdrive.multiblock.hyperdriveClass, mass)
		val warmup = (5.0 + log10(mass) * 2.0 + sqrt(speed.toDouble()) / 10.0).toInt()

		warmupTasks[starship] = HyperspaceWarmup(starship, warmup, dest, hyperdrive, useFuel)

		// create a new marker and add it to the collection
		if (starship.serverLevel.world == dest.world) {
			val marker = HyperspaceMarker(starship.centerOfMass.toLocation(starship.serverLevel.world), starship, dest)
			HyperspaceMap.addMarker(starship, marker)
		}

		(starship as? ActivePlayerStarship)?.pilot?.rewardAchievement(Achievement.USE_HYPERSPACE)
	}

	fun cancelJumpWarmup(warmup: HyperspaceWarmup) {
		check(warmupTasks.remove(warmup.ship, warmup)) { "Warmup wasn't in the map!" }
		val drive: HyperdriveSubsystem = warmup.drive
		if (drive.isIntact()) {
			drive.restoreFuel()
		}
		// remove hyperspace marker
		HyperspaceMap.deleteMarker(warmup.ship)
	}

	fun completeJumpWarmup(warmup: HyperspaceWarmup) {
		val starship = warmup.ship
		val originWorld = starship.serverLevel.world

		val players = starship.serverLevel.world
			.getNearbyPlayers(starship.centerOfMass.toLocation(starship.serverLevel.world), 2500.0)

		for (player in players) {
			player.playSound(
				Sound.sound(
					Key.key("minecraft:entity.elder_guardian.hurt"),
					Sound.Source.AMBIENT,
					5f,
					0.05f
				)
			)
		}
		Space.getPlanets()
			.filter { it.location.toLocation(starship.serverLevel.world).distance(starship.centerOfMass.toLocation(starship.serverLevel.world)) < 2500 }
			.filter { it.spaceWorld == starship.serverLevel.world }
			.forEach {
				it.planetWorld?.playSound(
					Sound.sound(
						Key.key("minecraft:entity.elder_guardian.hurt"),
						Sound.Source.AMBIENT,
						5f,
						0.05f
					)
				)
			}
		check(warmupTasks.remove(starship, warmup)) { "Warmup wasn't in the map!" }
		warmup.cancel()

		val world = getHyperspaceWorld(starship.serverLevel.world)
		val x = starship.centerOfMass.x.toDouble()
		val y = starship.centerOfMass.y.toDouble()
		val z = starship.centerOfMass.z.toDouble()
		val loc = Location(world, x, y, z)
		StarshipTeleportation.teleportStarship(starship, loc).thenAccept { success ->
			if (!success) {
				return@thenAccept
			}

			val mass = starship.mass
			val speed = calculateSpeed(warmup.drive.multiblock.hyperdriveClass, mass) / 10
			movementTasks[starship] = HyperspaceMovement(starship, speed, originWorld, warmup.dest)

			// Update the marker state (the ship went into hyperspace)
			val marker = HyperspaceMap.getMarker(starship)
			marker?.inHyperspace = true
			marker?.movement = movementTasks[starship]!!
		}
	}

	fun cancelJumpMovement(movement: HyperspaceMovement) {
		val starship = movement.ship

		check(movementTasks.remove(starship, movement)) { "Movement wasn't in the map!" }

		if (!ActiveStarships.isActive(starship)) {
			return
		}

		val world = getRealspaceWorld(starship.serverLevel.world)

		if (world == null) {
			starship.serverError("Failed to exit hyperspace: Realspace world not found")
			return
		}

		// Remove the marker from the map
		HyperspaceMap.deleteMarker(starship)

		val dest = starship.centerOfMass.toLocation(world)
		dest.x = movement.x
		dest.z = movement.z

		StarshipTeleportation.teleportStarship(starship, dest)
		for (player in movement.dest.world.getNearbyPlayers(movement.dest, 2500.0)) {
			player.playSound(Sound.sound(Key.key("minecraft:entity.warden.sonic_boom"), Sound.Source.AMBIENT, 1f, 0f))
		}
		Space.getPlanets().filter {
			it.location.toLocation(movement.dest.world).distance(movement.dest) < 2500
		}
			.forEach {
				it.planetWorld?.playSound(
					Sound.sound(
						Key.key("minecraft:entity.warden.sonic_boom"),
						Sound.Source.AMBIENT,
						1f,
						0f
					)
				)
			}
	}

	fun completeJumpMovement(movement: HyperspaceMovement) {
		val starship = movement.ship

		check(movementTasks.remove(starship, movement)) { "Movement wasn't in the map!" }

		movement.cancel()

		// Remove the marker from the map
		HyperspaceMap.deleteMarker(starship)

		StarshipTeleportation.teleportStarship(starship, movement.dest)
		starship.serverLevel.world.playSound(
			Sound.sound(
				Key.key("minecraft:entity.warden.sonic_boom"),
				Sound.Source.AMBIENT,
				1f,
				0f
			)
		)
		for (player in movement.dest.world.getNearbyPlayers(movement.dest, 2500.0)) {
			player.playSound(Sound.sound(Key.key("minecraft:entity.warden.sonic_boom"), Sound.Source.AMBIENT, 1f, 0f))
		}
		Space.getPlanets().filter {
			it.location.toLocation(movement.dest.world).distance(movement.dest) < 2500
		}
			.forEach {
				it.planetWorld?.playSound(
					Sound.sound(
						Key.key("minecraft:entity.warden.sonic_boom"),
						Sound.Source.AMBIENT,
						1f,
						0f
					)
				)
			}
	}

	private fun calculateSpeed(hyperdriveClass: Int, mass: Double) =
		(1500.0 / (log10(mass) / maxOf(1, hyperdriveClass)) + 100).toInt()

	/** returns the highest tier hyperdrive, with fuel if possible, or null */
	fun findHyperdrive(starship: ActiveStarship): HyperdriveSubsystem? = starship.hyperdrives.asSequence()
		.filter { it.isIntact() }
		.sortedBy { it.multiblock.hyperdriveClass }
		.sortedBy { it.hasFuel() }
		.lastOrNull()

	fun findHyperdrive(starship: ActiveStarship, tier: Int): HyperdriveSubsystem? = starship.hyperdrives.asSequence()
		.filter { it.isIntact() }
		.filter { it.multiblock.hyperdriveClass == tier }
		.sortedBy { it.hasFuel() }
		.lastOrNull()

	/** returns the intact nav computer with the highest range, or null */
	fun findNavComp(starship: ActiveStarship): NavCompSubsystem? = starship.navComps.asSequence()
		.filter { it.isIntact() }
		.sortedBy { it.multiblock.baseRange }
		.lastOrNull()

	fun isHyperspaceWorld(world: World): Boolean = world.name.endsWith("_hyperspace")

	fun getHyperspaceWorld(world: World): World? =
		if (!isHyperspaceWorld(world)) {
			Bukkit.getWorld(world.name + "_hyperspace")
		} else {
			null
		}

	fun getRealspaceWorld(world: World): World? =
		if (isHyperspaceWorld(world)) {
			Bukkit.getWorld(world.name.removeSuffix("_hyperspace"))
		} else {
			null
		}

	@EventHandler
	fun onStarshipActivated(event: StarshipActivatedEvent) {
		val starship = event.starship
		val world = starship.serverLevel.world

		if (!isHyperspaceWorld(world)) {
			return
		}

		val realspaceWorld = getRealspaceWorld(world) ?: return

		val dest = starship.centerOfMass.toLocation(realspaceWorld)
		StarshipTeleportation.teleportStarship(starship, dest)
	}

	@EventHandler
	fun onStarshipDeactivated(event: StarshipDeactivatedEvent) {
		val starship = event.starship
		warmupTasks.remove(starship)?.cancel()
		movementTasks.remove(starship)?.cancel()
	}

	@EventHandler
	fun onStarshipMove(event: StarshipMoveEvent) {
		if (!isHyperspaceWorld(event.starship.serverLevel.world)) {
			return
		}

		if (event.movement.newWorld != null) {
			return
		}

		event.isCancelled = true
	}

	@EventHandler
	fun onStarshipTranslate(event: StarshipTranslateEvent) {
		onStarshipMove(event)
	}

	@EventHandler
	fun onStarshipRotate(event: StarshipRotateEvent) {
		onStarshipMove(event)
	}

	fun getHyperspaceMovement(ship: ActivePlayerStarship): HyperspaceMovement? {
		return movementTasks[ship]
	}
}
