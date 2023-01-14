package net.horizonsend.ion.server.starships.subcraft

import io.papermc.paper.entity.RelativeTeleportFlag
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Rotation
import net.minecraft.world.level.block.state.BlockState
import net.starlegacy.database.schema.starships.SubCraftData
import net.starlegacy.feature.misc.CustomBlocks
import net.starlegacy.feature.starship.active.ActivePlayerStarship
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.active.ActiveStarships
import net.starlegacy.feature.starship.isFlyable
import net.starlegacy.feature.starship.movement.OptimizedMovement
import net.starlegacy.feature.starship.subsystem.DirectionalSubsystem
import net.starlegacy.feature.starship.subsystem.thruster.ThrustData
import net.starlegacy.util.nms
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.util.Vector
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

object SubShipUtils {
	fun execute(data: SubCraftData, blocks: LongOpenHashSet, clockwise: Boolean) {
		val origin: BlockPos = BlockPos.of(data.blockKey)
		val parent: ActiveStarship = ActiveStarships[data.parent] ?: return
		val world1: World = data.bukkitWorld()

		val oldLocationSet: LongOpenHashSet = blocks

		val oldLocationArray = oldLocationSet.filter {
			isFlyable(world1.getBlockAt(BlockPos.getX(it), BlockPos.getY(it), BlockPos.getZ(it)).blockData.nms)
		}.toLongArray()
		val newLocationArray = LongArray(oldLocationArray.size)

		val newLocationSet = LongOpenHashSet(oldLocationSet.size)

		for (i in oldLocationArray.indices) {
			val blockKey = oldLocationArray[i]
			val x0 = BlockPos.getX(blockKey)
			val y0 = BlockPos.getY(blockKey)
			val z0 = BlockPos.getZ(blockKey)

			val x = displaceX(x0, z0, origin, clockwise)
			val y = y0
			val z = displaceZ(z0, x0, origin, clockwise)

			val newBlockKey = BlockPos.asLong(x, y, z)
			newLocationArray[i] = newBlockKey

			newLocationSet.add(newBlockKey)
		}

		OptimizedMovement.moveBlockArray(
			world1,
			world1,
			oldLocationArray,
			newLocationArray,
			this::blockDataTransform
		) {
			parent.calculateMinMax()

			parent.subShips[data] = newLocationSet

			parent.blocks -= oldLocationSet
			parent.blocks += newLocationSet

			onComplete(data, clockwise)
		}
	}

	fun blockDataTransform(blockData: BlockState): BlockState =
		if (CustomBlocks[blockData] == null) {
			blockData.rotate(Rotation.CLOCKWISE_90)
		} else {
			blockData
		}

	fun displaceX(oldX: Int, oldZ: Int, origin: BlockPos, clockwise: Boolean): Int {
		val theta: Double = if (clockwise) 90.0 else -90.0
		val cosTheta: Double = cos(Math.toRadians(theta))
		val sinTheta: Double = sin(Math.toRadians(theta))
		val offsetX = oldX - origin.x
		val offsetZ = oldZ - origin.z
		return (offsetX.toDouble() * cosTheta - offsetZ.toDouble() * sinTheta).roundToInt() + origin.x
	}

	fun displaceZ(oldZ: Int, oldX: Int, origin: BlockPos, clockwise: Boolean): Int {
		val theta: Double = if (clockwise) 90.0 else -90.0
		val cosTheta: Double = cos(Math.toRadians(theta))
		val sinTheta: Double = sin(Math.toRadians(theta))
		val offsetX = oldX - origin.x
		val offsetZ = oldZ - origin.z
		return (offsetX.toDouble() * sinTheta + offsetZ.toDouble() * cosTheta).roundToInt() + origin.z
	}

	fun displaceLocation(oldLocation: Location, origin: BlockPos, clockwise: Boolean): Location {
		val theta: Double = if (clockwise) 90.0 else -90.0
		val cosTheta: Double = cos(Math.toRadians(theta))
		val sinTheta: Double = sin(Math.toRadians(theta))
		val centerX = origin.x + 0.5
		val centerZ = origin.z + 0.5
		val offsetX = oldLocation.x - centerX
		val offsetZ = oldLocation.z - centerZ
		val newX = offsetX * cosTheta - offsetZ * sinTheta
		val newZ = offsetX * sinTheta + offsetZ * cosTheta
		val newLocation = Location(oldLocation.world, newX + centerX, oldLocation.y, newZ + centerZ)
		newLocation.yaw += theta.toFloat()
		return newLocation
	}

	fun movePassenger(passenger: Entity, origin: BlockPos, clockwise: Boolean) {
		val newLoc = displaceLocation(passenger.location, origin, clockwise)
		if (passenger is Player) {
			newLoc.pitch = passenger.location.pitch
			newLoc.yaw += passenger.location.yaw
			passenger.teleport(newLoc, PlayerTeleportEvent.TeleportCause.PLUGIN, true, false, *RelativeTeleportFlag.values())
		} else {
			passenger.teleport(newLoc)
		}
	}

	fun rotateBlockFace(blockFace: BlockFace, clockwise: Boolean): BlockFace {
		return if (clockwise) {
			when (blockFace) {
				BlockFace.NORTH -> BlockFace.EAST
				BlockFace.EAST -> BlockFace.SOUTH
				BlockFace.SOUTH -> BlockFace.WEST
				BlockFace.WEST -> BlockFace.NORTH
				else -> blockFace
			}
		} else {
			when (blockFace) {
				BlockFace.NORTH -> BlockFace.WEST
				BlockFace.WEST -> BlockFace.SOUTH
				BlockFace.SOUTH -> BlockFace.EAST
				BlockFace.EAST -> BlockFace.NORTH
				else -> blockFace
			}
		}
	}

	private fun onComplete(data: SubCraftData, clockwise: Boolean) {
		val theta: Double = if (clockwise) 90.0 else -90.0
		val cosTheta: Double = cos(Math.toRadians(theta))
		val sinTheta: Double = sin(Math.toRadians(theta))
		val parent: ActiveStarship = ActiveStarships[data.parent] ?: return

		parent.calculateHitbox()
		for (subsystem in parent.subsystems) {
			if (subsystem is DirectionalSubsystem) {
				subsystem.face = rotateBlockFace(subsystem.face, clockwise)
			}
		}
		// rotate all the thruster data
		val thrusterMap: MutableMap<BlockFace, ThrustData> = parent.thrusterMap
		// creates a new map with the updated faces, then overwrites the old map
		// since the map contains every possible face, it overwrites every face
		thrusterMap.putAll(thrusterMap.mapKeys { (face: BlockFace, _) -> rotateBlockFace(face, clockwise) })

		parent.forwardBlockFace = rotateBlockFace(parent.forwardBlockFace, clockwise)

		if (parent is ActivePlayerStarship) {
			val dir = parent.cruiseData.targetDir
			if (dir != null) {
				val newX = dir.x * cosTheta - dir.z * sinTheta
				val newZ = dir.x * sinTheta + dir.z * cosTheta
				parent.cruiseData.targetDir = Vector(newX, dir.y, newZ)
			}
		}
	}
}
