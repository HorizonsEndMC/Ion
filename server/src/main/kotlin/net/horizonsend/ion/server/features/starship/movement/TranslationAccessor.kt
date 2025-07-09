package net.horizonsend.ion.server.features.starship.movement

import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.blockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.blockKeyX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.blockKeyY
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.blockKeyZ
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getY
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.rotateBlockFace
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.minecraft.world.level.block.Rotation
import net.minecraft.world.level.block.state.BlockState
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector
import java.util.function.Supplier
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

interface TranslationAccessor {
	val newWorld: World?

	fun displaceX(oldX: Int, oldZ: Int): Int
	fun displaceY(oldY: Int): Int
	fun displaceZ(oldZ: Int, oldX: Int): Int
	fun displaceLocation(oldLocation: Location): Location
	fun displaceFace(face: BlockFace): BlockFace
	fun displaceVector(vector: Vector): Vector
	fun displaceModernKey(key: BlockKey): BlockKey

	fun displaceLegacyKey(key: Long): Long {
		val oldX = blockKeyX(key)
		val oldY = blockKeyY(key)
		val oldZ = blockKeyZ(key)
		return blockKey(displaceX(oldX, oldZ), displaceY(oldY), displaceZ(oldZ, oldX))
	}

	fun displaceVec3i(vec: Vec3i): Vec3i {
		return Vec3i(displaceX(vec.x, vec.z), displaceY(vec.y), displaceZ(vec.z, vec.x))
	}

	fun blockStateTransform(blockState: BlockState): BlockState

	fun getNewPositionArray(oldPositions: LongArray): LongArray {
		return LongArray(oldPositions.size) { displaceLegacyKey(oldPositions[it]) }
	}

	fun execute(positions: LongArray, world1: World, executionCheck: () -> Boolean, callback: (LongArray) -> Unit) {
		val newPositions = getNewPositionArray(positions)

		OptimizedMovement.moveStarship(
			executionCheck = executionCheck,
			world1 = world1,
			world2 = newWorld ?: world1,
			oldPositionArray = positions,
			newPositionArray = newPositions,
			blockStateTransform = ::blockStateTransform,
			callback = { callback.invoke(newPositions) }
		)
	}

	class RotationTranslation(override val newWorld: World?, private val thetaDegrees: Double, val axisSupplier: Supplier<Vec3i>) : TranslationAccessor {
		val axis get() = axisSupplier.get()

		private val cosTheta: Double = cos(Math.toRadians(thetaDegrees))
		private val sinTheta: Double = sin(Math.toRadians(thetaDegrees))

		val nmsRotation =  when (if (thetaDegrees < 0) thetaDegrees + 90 else thetaDegrees % 360.0) {
			in -45.0..< 45.0 -> Rotation.NONE
			in 45.0..< 135.0 -> Rotation.CLOCKWISE_90
			in 135.0..< 225.0 -> Rotation.CLOCKWISE_180
			in 225.0..< 315.0 -> Rotation.COUNTERCLOCKWISE_90
			in 315.0.. 360.0 -> Rotation.NONE
			else -> throw IllegalArgumentException("something mod 360 returned more than 360?")
		}

		override fun displaceX(oldX: Int, oldZ: Int): Int {
			val offsetX = oldX - axis.x
			val offsetZ = oldZ - axis.z
			return (offsetX.toDouble() * cosTheta - offsetZ.toDouble() * sinTheta).roundToInt() + axis.x
		}

		override fun displaceY(oldY: Int): Int {
			return oldY
		}

		override fun displaceZ(oldZ: Int, oldX: Int): Int {
			val offsetX = oldX - axis.x
			val offsetZ = oldZ - axis.z
			return (offsetX.toDouble() * sinTheta + offsetZ.toDouble() * cosTheta).roundToInt() + axis.z
		}

		override fun blockStateTransform(blockState: BlockState): BlockState {
			val customBlock = CustomBlocks.getByBlockState(blockState)
			return if (customBlock == null) {
				blockState.rotate(nmsRotation)
			} else {
				CustomBlocks.getRotated(customBlock, blockState, nmsRotation)
			}
		}

		override fun displaceLocation(oldLocation: Location): Location {
			val centerX = axis.x + 0.5
			val centerZ = axis.z + 0.5
			val offsetX = oldLocation.x - centerX
			val offsetZ = oldLocation.z - centerZ
			val newX = offsetX * cosTheta - offsetZ * sinTheta
			val newZ = offsetX * sinTheta + offsetZ * cosTheta
			val newLocation = Location(oldLocation.world, newX + centerX, oldLocation.y, newZ + centerZ)
			newLocation.yaw += thetaDegrees.toFloat()
			newLocation.world = newWorld ?: newLocation.world
			return newLocation
		}

		override fun displaceFace(face: BlockFace): BlockFace {
			return rotateBlockFace(face, nmsRotation)
		}

		override fun displaceModernKey(key: BlockKey): BlockKey {
			val oldX = getX(key)
			val oldY = getY(key)
			val oldZ = getZ(key)

			return toBlockKey(
				displaceX(oldX, oldZ),
				displaceY(oldY),
				displaceZ(oldZ, oldX)
			)
		}

		override fun displaceVector(vector: Vector): Vector {
			return vector.clone().rotateAroundY(Math.toRadians(thetaDegrees) * PI)
		}
	}
}
