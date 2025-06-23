package net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary

import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.turret.TurretBaseMultiblock
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.movement.OptimizedMovement
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.horizonsend.ion.server.features.starship.movement.StarshipMovementException
import net.horizonsend.ion.server.features.starship.subsystem.DirectionalSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.StarshipSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getY
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.rotateBlockFace
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toLegacyBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.vectorToBlockFace
import net.horizonsend.ion.server.miscellaneous.utils.getBlockIfLoaded
import net.horizonsend.ion.server.miscellaneous.utils.leftFace
import net.horizonsend.ion.server.miscellaneous.utils.rightFace
import net.minecraft.world.level.block.Rotation
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector
import java.util.ArrayDeque
import java.util.LinkedList
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

class CustomTurretSubsystem(starship: Starship, pos: Vec3i, override var face: BlockFace) : StarshipSubsystem(starship, pos), DirectionalSubsystem {
	override fun isIntact(): Boolean {
		val block = getBlockIfLoaded(starship.world, pos.x, pos.y, pos.z) ?: return false
		return TurretBaseMultiblock.shape.checkRequirements(block, face, loadChunks = false, particles = false)
	}

	private var blocks = LongArray(0)
	private val captiveSubsystems = LinkedList<StarshipSubsystem>()

	fun ensureOriented(targetedDir: Vector): Boolean {
		val newFace = vectorToBlockFace(targetedDir)

		if (this.face == newFace) {
			return true
		}

		this.face = rotate(newFace)
		return this.face == newFace
	}

	fun detectTurret() {
		if (!starship.contains(pos.x, pos.y + 1, pos.z)) return

		val visitedBlocks = ObjectOpenHashSet<Block>()

		// Add the center of the turret base, it rotates with the turret to control direction.
		visitedBlocks.add(starship.world.getBlockAt(pos.x, pos.y, pos.z))

		val toVisit = ArrayDeque<Block>()

		toVisit.add(starship.world.getBlockAt(pos.x, pos.y + 1, pos.z))

		while (toVisit.isNotEmpty()) {
			val block = toVisit.removeFirst()

			if (!canDetect(block)) continue

			visitedBlocks.add(block)

			for (offsetX in -1..1) for (offsetY in -1..1) for (offsetZ in -1..1) {
				val newBlock = block.getRelative(offsetX, offsetY, offsetZ)

				if (block == newBlock) continue

				if (!starship.contains(newBlock.x, newBlock.y, newBlock.z)) continue

				if (visitedBlocks.contains(newBlock)) continue

				toVisit.addLast(newBlock)
			}
		}

		blocks = visitedBlocks.map { toBlockKey(it.x, it.y, it.z) }.toLongArray()
		starship.subsystems.filterTo(captiveSubsystems) { blocks.contains(toBlockKey(it.pos)) }
	}

	private fun canDetect(block: Block): Boolean {
		return block.y > pos.y
	}

	override fun onMovement(movement: StarshipMovement, success: Boolean) {
		if (!success) return
		blocks = LongArray(blocks.size) { movement.displaceKey(blocks[it]) }
	}

	fun rotate(newFace: BlockFace): BlockFace {
		if (starship.isTeleporting) return face
		val oldFace = face

		val i = when (newFace) {
			oldFace -> return oldFace
			oldFace.rightFace -> 1
			oldFace.oppositeFace -> 2
			oldFace.leftFace -> 3
			else -> error("Failed to calculate rotation iteration count from $oldFace to $newFace")
		}

		val theta: Double = 90.0 * i

		try {
			rotateBlocks(theta)
		} catch (e: StarshipMovementException) {
			return oldFace
		}

		return newFace
	}

	override fun tick() {
//		return
		rotateBlocks(90.0)
	}

	private fun rotateBlocks(thetaDegrees: Double) {
		val cosTheta: Double = cos(Math.toRadians(thetaDegrees))
		val sinTheta: Double = sin(Math.toRadians(thetaDegrees))

		val newPositionArray = LongArray(blocks.size)
		val newPositionSet = LongOpenHashSet(blocks.size)

		val nmsRotation = getNMSRotation(thetaDegrees)

		for (i in blocks.indices) {
			val blockKey = blocks[i]
			val x0 = getX(blockKey)
			val y0 = getY(blockKey)
			val z0 = getZ(blockKey)

			val x = displaceX(x0, z0, sinTheta, cosTheta)
			val z = displaceZ(z0, x0, sinTheta, cosTheta)

			val newBlockKey = toBlockKey(x, y0, z)
			newPositionArray[i] = newBlockKey

			newPositionSet.add(newBlockKey)
		}

		OptimizedMovement.moveStarship(
			executionCheck = { ActiveStarships.isActive(starship) },
			world1 = starship.world,
			world2 = starship.world,
			oldPositionArray = blocks.toLegacyBlockKey(),
			newPositionArray = newPositionArray.toLegacyBlockKey(),
			blockDataTransform = { blockState ->
				val customBlock = CustomBlocks.getByBlockState(blockState)

				if (customBlock == null) blockState.rotate(nmsRotation)
				else CustomBlocks.getRotated(customBlock, blockState, nmsRotation)
			}
		) {
			blocks = newPositionArray
			starship.blocks.removeAll(LongOpenHashSet(blocks.toLegacyBlockKey()))
			starship.blocks.addAll(LongOpenHashSet(newPositionArray.toLegacyBlockKey()))

			rotateCapturedSubsystems(sinTheta, cosTheta, nmsRotation)
		}
	}

	private fun rotateCapturedSubsystems(sinTheta: Double, cosTheta: Double, nmsRotation: Rotation) {
		for (subsystem in captiveSubsystems) {
			val oldX = subsystem.pos.x
			val oldZ = subsystem.pos.z
			subsystem.pos = Vec3i(displaceX(oldX, oldZ, sinTheta, cosTheta), subsystem.pos.y, displaceZ(oldZ, oldX, sinTheta, cosTheta))

			if (subsystem is DirectionalSubsystem) {
				subsystem.face = rotateBlockFace(subsystem.face, nmsRotation)
			}
		}
	}

	private fun displaceX(oldX: Int, oldZ: Int, sinTheta: Double, cosTheta: Double): Int {
		val offsetX = oldX - pos.x
		val offsetZ = oldZ - pos.z
		return (offsetX.toDouble() * cosTheta - offsetZ.toDouble() * sinTheta).roundToInt() + pos.x
	}

	private fun displaceZ(oldZ: Int, oldX: Int, sinTheta: Double, cosTheta: Double): Int {
		val offsetX = oldX - pos.x
		val offsetZ = oldZ - pos.z
		return (offsetX.toDouble() * sinTheta + offsetZ.toDouble() * cosTheta).roundToInt() + pos.z
	}

	companion object {
		private fun getNMSRotation(thetaDegrees: Double): Rotation {
			return when (thetaDegrees % 360.0) {
				in 0.0..< 45.0 -> Rotation.NONE
				in 45.0..< 135.0 -> Rotation.CLOCKWISE_90
				in 135.0..< 225.0 -> Rotation.CLOCKWISE_180
				in 225.0..< 315.0 -> Rotation.COUNTERCLOCKWISE_90
				in 315.0.. 360.0 -> Rotation.NONE
				else -> throw IllegalArgumentException("something mod 360 returned more than 360?")
			}
		}
	}
}
