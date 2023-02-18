package net.starlegacy.feature.starship.hyperspace

import net.horizonsend.ion.common.database.enums.Achievement
import net.horizonsend.ion.server.features.achievements.rewardAchievement
import net.horizonsend.ion.server.miscellaneous.extensions.serverError
import net.horizonsend.ion.server.miscellaneous.extensions.userError
import net.horizonsend.ion.server.miscellaneous.extensions.userErrorAction
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.starlegacy.SLComponent
import net.starlegacy.feature.space.Space
import net.starlegacy.feature.space.SpaceWorlds
import net.starlegacy.feature.starship.StarshipType.PLATFORM
import net.starlegacy.feature.starship.active.ActivePlayerStarship
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.active.ActiveStarships
import net.starlegacy.feature.starship.event.StarshipActivatedEvent
import net.starlegacy.feature.starship.event.StarshipDeactivatedEvent
import net.starlegacy.feature.starship.event.StarshipMoveEvent
import net.starlegacy.feature.starship.event.StarshipRotateEvent
import net.starlegacy.feature.starship.event.StarshipTranslateEvent
import net.starlegacy.feature.starship.movement.StarshipTeleportation
import net.starlegacy.feature.starship.subsystem.HyperdriveSubsystem
import net.starlegacy.feature.starship.subsystem.NavCompSubsystem
import net.starlegacy.util.toLocation
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.event.EventHandler
import kotlin.math.log10
import kotlin.math.sqrt

object Hyperspace : SLComponent() {
	private val warmupTasks = mutableMapOf<ActiveStarship, HyperspaceWarmup>()
	private val movementTasks = mutableMapOf<ActiveStarship, HyperspaceMovement>()

	fun isWarmingUp(starship: ActiveStarship) = warmupTasks.containsKey(starship)
	fun isMoving(starship: ActiveStarship) = movementTasks.containsKey(starship)

	const val HYPERMATTER_AMOUNT = 2

	override fun onDisable() {
		movementTasks.forEach { (_, hyperspaceMovement) ->
			cancelJumpMovement(hyperspaceMovement)
		}
	}

	fun beginJumpWarmup(starship: ActiveStarship, hyperdrive: HyperdriveSubsystem, x: Int, z: Int, useFuel: Boolean) {
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
		val dest = Location(spaceWorld, x.toDouble(), 128.0, z.toDouble())
		val mass = starship.mass
		val speed = calculateSpeed(hyperdrive.multiblock.hyperdriveClass, mass)
		val warmup = (5.0 + log10(mass) * 2.0 + sqrt(speed.toDouble()) / 10.0).toInt()
		warmupTasks[starship] = HyperspaceWarmup(starship, warmup, dest, hyperdrive, useFuel)

		(starship as? ActivePlayerStarship)?.pilot?.let { it.rewardAchievement(Achievement.USE_HYPERSPACE) }
	}

	fun cancelJumpWarmup(warmup: HyperspaceWarmup) {
		check(warmupTasks.remove(warmup.ship, warmup)) { "Warmup wasn't in the map!" }
		val drive: HyperdriveSubsystem = warmup.drive
		if (drive.isIntact()) {
			drive.restoreFuel()
		}
	}

	fun completeJumpWarmup(warmup: HyperspaceWarmup) {
		val starship = warmup.ship
		for (player in starship.serverLevel.world.getNearbyPlayers(starship.centerOfMass.toLocation(starship.serverLevel.world), 2500.0)) {
			player.playSound(
				Sound.sound(
					Key.key("minecraft:entity.elder_guardian.hurt"),
					Sound.Source.AMBIENT,
					5f,
					0.05f
				)
			)
		}
		Space.getPlanets().filter {
			it.location.toLocation(starship.serverLevel.world).distance(starship.centerOfMass.toLocation(starship.serverLevel.world)) < 2500
		}
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
			movementTasks[starship] = HyperspaceMovement(starship, speed, warmup.dest)
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
		(1500.0 / (log10(mass) / hyperdriveClass) + 100).toInt()

	/** returns the highest tier hyperdrive, with fuel if possible, or null */
	fun findHyperdrive(starship: ActiveStarship): HyperdriveSubsystem? = starship.hyperdrives.asSequence()
		.filter { it.isIntact() }
		.sortedBy { it.multiblock.hyperdriveClass }
		.sortedBy { it.hasFuel() }
		.lastOrNull()

	/** returns the intact nav computer with the highest range, or null */
	fun findNavComp(starship: ActiveStarship): NavCompSubsystem? = starship.navComps.asSequence()
		.filter { it.isIntact() }
		.sortedBy { it.multiblock.baseRange }
		.lastOrNull()

	fun isHyperspaceWorld(world: World): Boolean = world.name.endsWith("_Hyperspace")

	fun getHyperspaceWorld(world: World): World? =
		if (!isHyperspaceWorld(world)) {
			Bukkit.getWorld(world.name + "_Hyperspace")
		} else {
			null
		}

	fun getRealspaceWorld(world: World): World? =
		if (isHyperspaceWorld(world)) {
			Bukkit.getWorld(world.name.removeSuffix("_Hyperspace"))
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
}
