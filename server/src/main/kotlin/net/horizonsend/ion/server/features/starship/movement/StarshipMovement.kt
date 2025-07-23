package net.horizonsend.ion.server.features.starship.movement

import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.horizonsend.ion.common.database.schema.Cryopod
import net.horizonsend.ion.common.database.schema.starships.StarshipData
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.serverError
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.player.CombatTimer
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.space.body.planet.CachedPlanet
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.features.starship.TypeCategory
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.event.EnterPlanetEvent
import net.horizonsend.ion.server.features.starship.isFlyable
import net.horizonsend.ion.server.features.starship.subsystem.misc.CryopodSubsystem
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.listener.misc.ProtectionListener
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.blockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.blockKeyX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.blockKeyY
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.blockKeyZ
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.rectangle
import net.horizonsend.ion.server.miscellaneous.utils.isShulkerBox
import net.horizonsend.ion.server.miscellaneous.utils.nms
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Animals
import org.bukkit.entity.Entity
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import org.litote.kmongo.setValue
import kotlin.math.sqrt

abstract class StarshipMovement(val starship: ActiveStarship) : TranslationAccessor {
	// null if the ship is not a player ship
	private val playerShip: Starship = starship

	protected abstract fun movePassenger(passenger: Entity)
	protected abstract fun onComplete()

	/* should only be called by the ship itself */
	fun execute() {
		val world1: World = starship.world
		val world2 = newWorld ?: world1

		val oldLocationSet: LongOpenHashSet = starship.blocks

		check(newWorld != world1) { "New world can't be the same as the current world" }

		if (!starship.type.canPilotIn(world2.ion)) {
			throw StarshipMovementException("Ships of this class can't be piloted in ${world2.name}")
		}

		if (!ActiveStarships.isActive(starship)) {
			starship.serverError("Starship not active, movement cancelled.")
			Throwable().printStackTrace()
			return
		}

		if (displaceY(starship.min.y) < 0) {
			throw StarshipOutOfBoundsException("Minimum height limit reached")
		}

		if (displaceY(starship.max.y) >= world1.maxHeight) {
			if (playerShip.type != StarshipType.SPEEDER && exitPlanet(world1, playerShip)) {
				starship.information("Exiting Planet")
				return
			}

			throw StarshipOutOfBoundsException("Maximum height limit reached")
		}

		validateWorldBorders(starship.min, starship.max, findPassengers(world1), world2)
		checkCelestialBodies(starship.min, starship.max, world2)
		checkEnteringSafeZone(starship.min, starship.max, starship, world2)

		val oldLocationArray = oldLocationSet.filter {
			isFlyable(world1.getBlockAt(blockKeyX(it), blockKeyY(it), blockKeyZ(it)).blockData.nms)
		}.toLongArray()
		val newLocationArray = LongArray(oldLocationArray.size)

		val newLocationSet = LongOpenHashSet(oldLocationSet.size)

		for (i in oldLocationArray.indices) {
			val blockKey = oldLocationArray[i]
			val x0 = blockKeyX(blockKey)
			val y0 = blockKeyY(blockKey)
			val z0 = blockKeyZ(blockKey)

			val x = displaceX(x0, z0)
			val y = displaceY(y0)
			val z = displaceZ(z0, x0)

			val newBlockKey = blockKey(x, y, z)
			newLocationArray[i] = newBlockKey

			newLocationSet.add(newBlockKey)
		}

		OptimizedMovement.moveStarship(
			executionCheck = { ActiveStarships.isActive(starship) },
			world1 = world1,
			world2 = world2,
			oldPositionArray = oldLocationArray,
			newPositionArray = newLocationArray,
			blockStateTransform = this::blockStateTransform
		) {
			// this part will run on the main thread
			movePassengers(findPassengers(world1))

			starship.world = world2
			starship.blocks = newLocationSet
			moveShipComputers(world2)
			updateDirectControlCenter()
			moveDisconnectLocation()
			starship.calculateMinMax()
			updateCenter()
			updateSubsystems(world2)
			starship.multiblockManager.displace(this)
			starship.transportManager.displace(this)

			onComplete()
		}

		if (world1 != world2 && !world2.toString().contains("hyperspace", ignoreCase=true)) {
			EnterPlanetEvent(world1, world2, starship.controller).callEvent()
		}
	}

	private fun findPassengers(world1: World): List<Entity> {
		val passengerChunks = starship.blocks.clone()
			.mapTo(mutableSetOf()) { world1.getChunkAt(blockKeyX(it) shr 4, blockKeyZ(it) shr 4) }

		val passengers = mutableSetOf<Entity>()

		passengers.addAll(starship.onlinePassengers)

		for (chunk in passengerChunks) for (entity in chunk.entities) {
			if (passengers.contains(entity)) continue

			when (entity) {
				is Player -> if (starship.isWithinHitbox(entity) && ActiveStarships.findByPassenger(entity) == null) passengers.add(entity)

				is Animals -> if (starship.isWithinHitbox(entity)) passengers.add(entity)

				is Item -> if (starship.isWithinHitbox(entity) && !entity.itemStack.type.isShulkerBox) passengers.add(entity)
			}
		}

		return passengers.toList()
	}

	private fun validateWorldBorders(min: Vec3i, max: Vec3i, passengers: List<Entity>, world2: World) {
		val newMin = displaceVec3i(min).toLocation(world2)
		val newMax = displaceVec3i(max).toLocation(world2)

		if (!world2.worldBorder.isInside(newMin) || !world2.worldBorder.isInside(newMax))
			// Handle cases where there are no pilots
			throw StarshipOutOfBoundsException("Starship would be outside the world border!")

		for (passenger: Entity in passengers) {
			val newLoc: Location = displaceLocation(passenger.location)
			newLoc.world = world2

			if (world2.worldBorder.isInside(newLoc)) {
				continue
			}

			throw StarshipOutOfBoundsException(
				"You're too close to the world border! " +
					"${passenger.name} would be outside of it at ${Vec3i(newLoc)}."
			)
		}
	}

	private fun checkCelestialBodies(min: Vec3i, max: Vec3i, world2: World) {
		val newMin = displaceVec3i(min).toLocation(world2)
		val newMax = displaceVec3i(max).toLocation(world2)

		val stars = Space.getStars().filter { it.spaceWorld?.uid == world2.uid }

		for (star in stars) {
			val planetLoc = star.location.toLocation(world2)

			val distance1 = newMin.toLocation(world2).distance(planetLoc)
			val distance2 = newMax.toLocation(world2).distance(planetLoc)

			if (distance1 < star.outerSphereRadius || distance2 < star.outerSphereRadius)
				throw StarshipOutOfBoundsException("Starship would be inside ${star.name}!")
		}
	}

	private fun checkEnteringSafeZone(min: Vec3i, max: Vec3i, starship: Starship, world2: World) {

		if (starship.controller !is PlayerController) return

		val newMin = displaceVec3i(min).toLocation(world2)
		val newMax = displaceVec3i(max).toLocation(world2)

		val oldBoundingBox = rectangle(min.toLocation(world2), max.toLocation(world2))
		val newBoundingBox = rectangle(newMin, newMax)

		// if any point of the old starship's bounding box was inside a trade city, do not perform this check
		// this should allow for ships that are combat tagged to leave a city, but not return
		for (point in oldBoundingBox) {
			if (ProtectionListener.isProtectedCity(point)) return
		}

		for (point in newBoundingBox) {
			if (ProtectionListener.isProtectedCity(point) && starship.type.typeCategory == TypeCategory.WAR_SHIP &&
				CombatTimer.isPvpCombatTagged((starship.controller as PlayerController).player)) {

				throw StarshipOutOfBoundsException("The trade city denies your starship entry for your recent acts of aggression!")
			}
		}
	}

	private fun moveShipComputers(world2: World) {
		moveSelfComputer(world2)
		moveCarriedShipComputers(world2)
	}

	private fun moveSelfComputer(world2: World) {
		val data = playerShip.data
		ActiveStarships.updateLocation(data, world2, displaceLegacyKey(data.blockKey))
	}

	private fun moveCarriedShipComputers(world2: World) {
		for (data: StarshipData in playerShip.carriedShips.keys) {
			data.blockKey = displaceLegacyKey(data.blockKey)
			data.levelName = world2.name

			val blocks = playerShip.carriedShips[data] ?: continue // the rest is only for the carried ships
			playerShip.carriedShips[data] = blocks.mapTo(LongOpenHashSet(blocks.size)) { key: Long ->
				displaceLegacyKey(key)
			}
		}
	}

	private fun moveDisconnectLocation() {
		val disconnectLoc = starship.pilotDisconnectLocation ?: return

		starship.pilotDisconnectLocation = displaceVec3i(disconnectLoc)
	}

	private fun updateDirectControlCenter() {
		val directControlCenter = playerShip?.directControlCenter ?: return
		playerShip.directControlCenter = displaceLocation(directControlCenter)
	}

	private fun updateCenter() {
		val oldCenter = starship.centerOfMass
		val newCenterX = displaceX(oldCenter.x, oldCenter.z)
		val newCenterZ = displaceZ(oldCenter.z, oldCenter.x)
		starship.centerOfMass = Vec3i(newCenterX, displaceY(oldCenter.y), newCenterZ)
	}

	private fun updateSubsystems(world2: World) {
		for (subsystem in starship.subsystems) {
			val newPos = displaceVec3i(subsystem.pos)
			subsystem.pos = newPos

			if (subsystem is CryopodSubsystem) Tasks.async {
				Cryopod.updateById(
					subsystem.pod._id,
					setValue(Cryopod::x, newPos.x),
					setValue(Cryopod::y, newPos.y),
					setValue(Cryopod::z, newPos.z),
					setValue(Cryopod::worldName, world2.name)
				)
			}
		}
	}

	private fun exitPlanet(world: World, starship: ActiveControlledStarship): Boolean {
		if (starship.isTeleporting) return false

		val planet: CachedPlanet = Space.getPlanet(world) ?: return false

		val border = planet.planetWorld?.worldBorder
			?.takeIf { it.size < 60_000_000 } // don't use if it's the default, giant border
		val halfLength = if (border == null) 2500.0 else border.size / 2.0
		val centerX = border?.center?.x ?: halfLength
		val centerZ = border?.center?.z ?: halfLength

		val shipCenter = starship.centerOfMass.toVector().setY(0)

		val direction: Vector = shipCenter.clone().subtract(Vector(centerX, 0.0, centerZ))
		direction.setY(0)
		direction.normalize()

		val spaceWorld = planet.spaceWorld
		if (spaceWorld == null) {
			starship.serverError("World ${planet.spaceWorldName} not found")
			return false
		}

		// Don't allow players that have recently exited planets to re-exit again
		val controller = starship.controller
		if (controller is PlayerController) {
			if (PlanetTeleportCooldown.cannotExitPlanets(controller.player)) return false
		}

		val blockCountSquareRoot = sqrt(starship.initialBlockCount.toDouble())
		val distance: Double = 15 + (planet.atmosphereRadius + blockCountSquareRoot) * 1.5

		val exitPoint: Location = planet.location
			.toLocation(spaceWorld)
			.add(direction.x * distance, 0.0, direction.z * distance)

		StarshipTeleportation.teleportStarship(starship, exitPoint) {
			if (controller is PlayerController) {
				// (Callback) If planet teleportation was successful, add exit planet restriction (if applicable)
				PlanetTeleportCooldown.addExitPlanetRestriction(controller.player)
			}
		}

		IonServer.slF4JLogger.info("Attempting to exit planet")

		return true
	}

	private fun movePassengers(passengers: List<Entity>) {
		for (passenger: Entity in passengers) {
			movePassenger(passenger)
		}
	}
}
