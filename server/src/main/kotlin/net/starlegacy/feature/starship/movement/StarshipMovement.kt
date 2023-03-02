package net.starlegacy.feature.starship.movement

import co.aikar.commands.ConditionFailedException
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.horizonsend.ion.server.features.starship.mininglaser.MiningLaserSubsystem
import net.horizonsend.ion.server.legacy.events.EnterPlanetEvent
import net.horizonsend.ion.server.miscellaneous.minecraft
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.state.BlockState
import net.starlegacy.database.schema.starships.PlayerStarshipData
import net.starlegacy.feature.misc.CryoPods
import net.starlegacy.feature.space.CachedPlanet
import net.starlegacy.feature.space.Space
import net.starlegacy.feature.starship.active.ActivePlayerStarship
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.active.ActiveStarships
import net.starlegacy.feature.starship.isFlyable
import net.starlegacy.feature.starship.subsystem.CryoSubsystem
import net.starlegacy.util.Vec3i
import net.starlegacy.util.blockKey
import net.starlegacy.util.blockKeyX
import net.starlegacy.util.blockKeyY
import net.starlegacy.util.blockKeyZ
import net.starlegacy.util.nms
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Animals
import org.bukkit.entity.EnderCrystal
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import kotlin.collections.set
import kotlin.math.sqrt

abstract class StarshipMovement(val starship: ActiveStarship, val newWorld: World? = null) {
	// null if the ship is not a player ship
	private val playerShip: ActivePlayerStarship? = starship as? ActivePlayerStarship

	protected abstract fun displaceX(oldX: Int, oldZ: Int): Int
	protected abstract fun displaceY(oldY: Int): Int
	protected abstract fun displaceZ(oldZ: Int, oldX: Int): Int
	protected abstract fun displaceLocation(oldLocation: Location): Location
	protected abstract fun movePassenger(passenger: Entity)
	protected abstract fun onComplete()
	protected abstract fun blockDataTransform(blockData: BlockState): BlockState

	/* should only be called by the ship itself */
	fun execute() {
		val world1: World = starship.serverLevel.world
		val world2 = newWorld ?: world1

		val oldLocationSet: LongOpenHashSet = starship.blocks

		check(newWorld != world1) { "New world can't be the same as the current world" }

		if (!ActiveStarships.isActive(starship)) {
			starship.sendMessage("&cStarship not active, movement cancelled.")
			return
		}

		if (displaceY(starship.min.y) < 0) {
			throw ConditionFailedException("Minimum height limit reached")
		}

		if (displaceY(starship.max.y) >= world1.maxHeight) {
			if (playerShip != null && exitPlanet(world1, playerShip)) {
				starship.sendMessage("&7Exiting planet")
				return
			}

			throw ConditionFailedException("Maximum height limit reached")
		}

		validateWorldBorders(findPassengers(world1), world2)

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

			starship.serverLevel = world2.minecraft
			starship.blocks = newLocationSet
			moveShipComputers(world2)
			updateDirectControlCenter()
			starship.calculateMinMax()
			updateCenter()
			updateSubsystems(world2)

			onComplete()
		}
		if (world1 != world2 && !world2.toString().contains("hyperspace")) {
			EnterPlanetEvent(world1, world2, (starship as? ActivePlayerStarship)!!.pilot!!).callEvent()
		}
	}

	private fun findPassengers(world1: World): List<Entity> {
		val passengerChunks = starship.blocks
			.map { world1.getChunkAt(blockKeyX(it) shr 4, blockKeyZ(it) shr 4) }
			.toSet()
		val passengers = mutableSetOf<Entity>()
		passengers.addAll(starship.onlinePassengers)
		for (chunk in passengerChunks) {
			for (entity in chunk.entities) {
				if (passengers.contains(entity)) {
					continue
				}
				when (entity) {
					is Player -> {
						if (starship.isWithinHitbox(entity) && ActiveStarships.findByPassenger(entity) == null) {
							passengers.add(entity)
						}
					}

					is Animals -> {
						if (starship.isWithinHitbox(entity)) {
							passengers.add(entity)
						}
					}

					is EnderCrystal -> {
						if (starship.isWithinHitbox(entity)) {
							passengers.add(entity)
						}
					}
				}
			}
		}

		return passengers.toList()
	}

	private fun validateWorldBorders(passengers: List<Entity>, world2: World) {
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

		for (data: PlayerStarshipData in playerShip.carriedShips.keys) {
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
		starship.centerOfMass = BlockPos(newCenterX, displaceY(oldCenter.y), newCenterZ)
	}

	private fun updateSubsystems(world2: World) {
		for (subsystem in starship.subsystems) {
			val newPos = displacedVec(subsystem.pos)
			subsystem.pos = newPos
			if (subsystem is CryoSubsystem) {
				val pod = subsystem.pod
				if (pod.world != world2.name || pod.pos != newPos) {
					if (CryoPods.isSelected(pod)) {
						CryoPods.setCryoPod(pod.player, world2.name, newPos)
					}
					subsystem.pod = CryoPods.CryoPod(pod.player, world2.name, newPos)
				}
			}
		}
	}

	private fun exitPlanet(world: World, starship: ActivePlayerStarship): Boolean {
		val planet: CachedPlanet = Space.getPlanet(world) ?: return false
		val pilot: Player = starship.pilot ?: return false
		val direction: Vector = pilot.location.direction
		direction.setY(0)
		direction.normalize()

		val spaceWorld = planet.spaceWorld
		if (spaceWorld == null) {
			starship.sendMessage("&cWorld ${planet.spaceWorldName} not found")
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
