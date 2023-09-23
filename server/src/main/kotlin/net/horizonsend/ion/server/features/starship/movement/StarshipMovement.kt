package net.horizonsend.ion.server.features.starship.movement

import co.aikar.commands.ConditionFailedException
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.horizonsend.ion.common.database.schema.Cryopod
import net.horizonsend.ion.common.database.schema.starships.StarshipData
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.serverError
import net.horizonsend.ion.server.features.space.CachedPlanet
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.event.EnterPlanetEvent
import net.horizonsend.ion.server.features.starship.isFlyable
import net.horizonsend.ion.server.features.starship.subsystem.CryoSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.blockKey
import net.horizonsend.ion.server.miscellaneous.utils.blockKeyX
import net.horizonsend.ion.server.miscellaneous.utils.blockKeyY
import net.horizonsend.ion.server.miscellaneous.utils.blockKeyZ
import net.horizonsend.ion.server.miscellaneous.utils.nms
import net.minecraft.world.level.block.state.BlockState
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Animals
import org.bukkit.entity.Entity
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import org.litote.kmongo.setValue
import kotlin.collections.set
import kotlin.math.sqrt

abstract class StarshipMovement(val starship: ActiveStarship, val newWorld: World? = null) {
	// null if the ship is not a player ship
	private val playerShip: ActiveControlledStarship? = starship as? ActiveControlledStarship

	protected abstract fun displaceX(oldX: Int, oldZ: Int): Int
	protected abstract fun displaceY(oldY: Int): Int
	protected abstract fun displaceZ(oldZ: Int, oldX: Int): Int
	protected abstract fun displaceLocation(oldLocation: Location): Location
	protected abstract fun movePassenger(passenger: Entity)
	protected abstract fun onComplete()
	protected abstract fun blockDataTransform(blockData: BlockState): BlockState

	/* should only be called by the ship itself */
	fun execute() {
		val world1: World = starship.world
		val world2 = newWorld ?: world1

		val oldLocationSet: LongOpenHashSet = starship.blocks

		check(newWorld != world1) { "New world can't be the same as the current world" }

		if (!ActiveStarships.isActive(starship)) {
			starship.serverError("Starship not active, movement cancelled.")
			return
		}

		if (displaceY(starship.min.y) < 0) {
			throw ConditionFailedException("Minimum height limit reached")
		}

		if (displaceY(starship.max.y) >= world1.maxHeight) {
			if (playerShip != null && exitPlanet(world1, playerShip)) {
				starship.information("Exiting Planet")
				return
			}

			throw ConditionFailedException("Maximum height limit reached")
		}

		validateWorldBorders(starship.centerOfMass, findPassengers(world1), world2)

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
			starship,
			world1,
			world2,
			oldLocationArray,
			newLocationArray,
			this::blockDataTransform
		) {
			// this part will run on the main thread
			movePassengers(findPassengers(world1))

			starship.world = world2
			starship.blocks = newLocationSet
			moveShipComputers(world2)
			updateDirectControlCenter()
			starship.calculateMinMax()
			updateCenter()
			updateSubsystems(world2)

			onComplete()
		}

		if (world1 != world2 && !world2.toString().contains("hyperspace", ignoreCase=true)) {
			EnterPlanetEvent(world1, world2, starship.controller).callEvent()
		}
	}

	private fun findPassengers(world1: World): List<Entity> {
		val passengerChunks = starship.blocks
			.map { world1.getChunkAt(blockKeyX(it) shr 4, blockKeyZ(it) shr 4) }
			.toSet()

		val passengers = mutableSetOf<Entity>()

		passengers.addAll(starship.onlinePassengers)

		for (chunk in passengerChunks) for (entity in chunk.entities) {
			if (passengers.contains(entity)) continue

			when (entity) {
				is Player -> if (starship.isWithinHitbox(entity) && ActiveStarships.findByPassenger(entity) == null) passengers.add(entity)

				is Animals -> if (starship.isWithinHitbox(entity)) passengers.add(entity)

				is Item -> if (starship.isWithinHitbox(entity)) passengers.add(entity)
			}
		}

		return passengers.toList()
	}

	private fun validateWorldBorders(centerOfMass: Vec3i, passengers: List<Entity>, world2: World) {
		if (world2.worldBorder.isInside(centerOfMass.toLocation(world2)))
			// Handle cases where there are no pilots
			throw ConditionFailedException("Starship would be outside the world border!")

		for (passenger: Entity in passengers) {
			val newLoc: Location = displaceLocation(passenger.location)
			newLoc.world = world2

			if (world2.worldBorder.isInside(newLoc)) {
				continue
			}

			throw ConditionFailedException(
				"You're too close to the world border! " +
					"${passenger.name} would be outside of it at ${Vec3i(newLoc)}."
			)
		}
	}

	private fun moveShipComputers(world2: World) {
		moveSelfComputer(world2)
		moveCarriedShipComputers(world2)
	}

	private fun displacedVec(vec: Vec3i): Vec3i {
		return Vec3i(displaceX(vec.x, vec.z), displaceY(vec.y), displaceZ(vec.z, vec.x))
	}

	private fun displacedKey(key: Long): Long {
		val oldX = blockKeyX(key)
		val oldY = blockKeyY(key)
		val oldZ = blockKeyZ(key)
		return blockKey(displaceX(oldX, oldZ), displaceY(oldY), displaceZ(oldZ, oldX))
	}

	private fun moveSelfComputer(world2: World) {
		if (playerShip == null) {
			return
		}

		val data = playerShip.data
		ActiveStarships.updateLocation(data, world2, displacedKey(data.blockKey))
	}

	private fun moveCarriedShipComputers(world2: World) {
		if (playerShip == null) {
			return
		}

		for (data: StarshipData in playerShip.carriedShips.keys) {
			data.blockKey = displacedKey(data.blockKey)
			data.levelName = world2.name

			val blocks = playerShip.carriedShips[data] ?: continue // the rest is only for the carried ships
			playerShip.carriedShips[data] = blocks.mapTo(LongOpenHashSet(blocks.size)) { key: Long ->
				displacedKey(key)
			}
		}
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
		CoroutineScope(Dispatchers.Default + SupervisorJob()).launch {
			for (subsystem in starship.subsystems) {
				launch {
					val newPos = displacedVec(subsystem.pos)
					subsystem.pos = newPos

					if (subsystem is CryoSubsystem) {
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
		}
	}

	private fun exitPlanet(world: World, starship: ActiveControlledStarship): Boolean {
		val planet: CachedPlanet = Space.getPlanet(world) ?: return false
		val pilot: Player = starship.playerPilot ?: return false
		val direction: Vector = pilot.location.direction
		direction.setY(0)
		direction.normalize()

		val spaceWorld = planet.spaceWorld
		if (spaceWorld == null) {
			starship.serverError("World ${planet.spaceWorldName} not found")
			return false
		}

		val blockCountSquareRoot = sqrt(starship.initialBlockCount.toDouble())
		val distance: Double = 15 + (planet.atmosphereRadius + blockCountSquareRoot) * 1.5

		val exitPoint: Location = planet.location
			.toLocation(spaceWorld)
			.add(direction.x * distance, 0.0, direction.z * distance)

		StarshipTeleportation.teleportStarship(starship, exitPoint)
		return true
	}

	private fun movePassengers(passengers: List<Entity>) {
		for (passenger: Entity in passengers) {
			movePassenger(passenger)
		}
	}
}
