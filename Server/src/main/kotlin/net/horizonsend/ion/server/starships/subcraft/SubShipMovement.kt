package net.horizonsend.ion.server.starships.subcraft

import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.horizonsend.ion.server.legacy.feedback.FeedbackType
import net.horizonsend.ion.server.legacy.feedback.sendFeedbackMessage
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
import org.bukkit.util.Vector
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

open class SubShipMovement(val data: SubCraftData, private var oldLocationSet: LongOpenHashSet, private val clockwise: Boolean) {
	private val origin = BlockPos.of(data.blockKey)
	private val nmsRotation = if (clockwise) Rotation.CLOCKWISE_90 else Rotation.COUNTERCLOCKWISE_90
	private val theta: Double = if (clockwise) 90.0 else -90.0
	private val cosTheta: Double = cos(Math.toRadians(theta))
	private val sinTheta: Double = sin(Math.toRadians(theta))
	val parent: ActiveStarship? = ActiveStarships[data.parent]

	open fun execute() {
		if (parent == null) return

		val world1: World = parent.serverLevel.world

		if (!ActiveStarships.isActive(parent)) {
			parent.sendFeedbackMessage(FeedbackType.INFORMATION, "Starship not active, movement cancelled.")
			return
		}

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

			val x = displaceX(x0, z0)
			val z = displaceZ(z0, x0)

			val newBlockKey = BlockPos.asLong(x, y0, z)
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
			// this part will run on the main thread
			parent.blocks -= oldLocationSet
			parent.blocks += newLocationSet

			parent.calculateMinMax()
			parent.subShips[data] = newLocationSet

			onComplete()

			oldLocationSet = newLocationSet
		}
	}

	fun blockDataTransform(blockData: BlockState): BlockState =
		if (CustomBlocks[blockData] == null) {
			blockData.rotate(nmsRotation)
		} else {
			blockData
		}

	fun displaceX(oldX: Int, oldZ: Int): Int {
		val offsetX = oldX - origin.x
		val offsetZ = oldZ - origin.z
		return (offsetX.toDouble() * cosTheta - offsetZ.toDouble() * sinTheta).roundToInt() + origin.x
	}

	fun displaceZ(oldZ: Int, oldX: Int): Int {
		val offsetX = oldX - origin.x
		val offsetZ = oldZ - origin.z
		return (offsetX.toDouble() * sinTheta + offsetZ.toDouble() * cosTheta).roundToInt() + origin.z
	}

	fun displaceLocation(oldLocation: Location): Location {
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

	private fun rotateBlockFace(blockFace: BlockFace): BlockFace {
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

	fun onComplete() {
		if (parent == null) return
		parent.calculateHitbox()

		for (subsystem in parent.subsystems.filter { parent.subShips[data]?.contains(it.pos.toBlockPos().asLong()) ?: false }) {
			if (subsystem is DirectionalSubsystem) {
				subsystem.face = rotateBlockFace(subsystem.face)
			}
		}
		// rotate all the thruster data
		val thrusterMap: MutableMap<BlockFace, ThrustData> = parent.thrusterMap
		// creates a new map with the updated faces, then overwrites the old map
		// since the map contains every possible face, it overwrites every face
		thrusterMap.putAll(thrusterMap.mapKeys { (face: BlockFace, _) -> rotateBlockFace(face) })

		parent.forwardBlockFace = rotateBlockFace(parent.forwardBlockFace)

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
