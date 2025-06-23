package net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.highlightBlock
import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.turret.TurretBaseMultiblock
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.active.ActiveStarshipFactory
import net.horizonsend.ion.server.features.starship.movement.OptimizedMovement
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.horizonsend.ion.server.features.starship.movement.StarshipMovementException
import net.horizonsend.ion.server.features.starship.subsystem.DirectionalSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.StarshipSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.TurretWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.blockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getY
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.rotateBlockFace
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toLegacyBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.vectorToBlockFace
import net.horizonsend.ion.server.miscellaneous.utils.debugAudience
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
	companion object {
		val disallowedSubsystems = setOf(CustomTurretSubsystem::class, TurretWeaponSubsystem::class)
	}

	override fun isIntact(): Boolean {
		// Only check the base
		val block = getBlockIfLoaded(starship.world, pos.x, pos.y, pos.z) ?: return false
		return TurretBaseMultiblock.shape.checkRequirements(block, face, loadChunks = false, particles = false)
	}

	private var blocks = LongArray(0)
	private var captiveSubsystems = LinkedList<StarshipSubsystem>()

	fun orientToTarget(targetedDir: Vector): Boolean {
		val newFace = vectorToBlockFace(targetedDir)
		if (this.face == newFace) return true

		return rotate(newFace)
	}

	fun detectTurret() {
		val starshipBlocks = starship.blocks
		val subsystemsByPos = starship.subsystems.associateByTo(Long2ObjectOpenHashMap()) { toBlockKey(it.pos) }
		if (!starship.contains(pos.x, pos.y + 1, pos.z)) return // Turret base is empty

		// Add the center of the turret base, it rotates with the turret to control direction.
		val foundBlocks = LongOpenHashSet.of()
		val foundSubsystems = ObjectOpenHashSet<StarshipSubsystem>()

		// Queue of blocks that need to be visited
		val visitQueue = ArrayDeque<Vec3i>()
		// Set of all blocks that have been visited
		val visitSet = LongOpenHashSet()

		// Jump to start with the origin
		visitQueue.add(Vec3i(pos.x, pos.y + 1, pos.z))

		var iterations = 0L

		// Perform a flood fill to find turret blocks
		while (visitQueue.isNotEmpty()) {
			iterations++
			val currentVec3i = visitQueue.removeFirst()
			val currentKey = toBlockKey(currentVec3i.x, currentVec3i.y, currentVec3i.z)

			if (!starshipBlocks.contains(blockKey(currentVec3i.x, currentVec3i.y, currentVec3i.z))) continue

			// Block can be a part of the turret
			foundBlocks.add(currentKey)

			val subsystem = subsystemsByPos[currentKey]
			subsystem?.let {
				if (disallowedSubsystems.contains(it::class)) throw ActiveStarshipFactory.StarshipActivationException("${subsystem.javaClass.simpleName}s cannot be part of custom turrets!")
				foundSubsystems.add(it)
			}

			for (offsetX in -1..1) for (offsetY in -1..1) for (offsetZ in -1..1) {
				iterations++
				val newVec = currentVec3i.plus(Vec3i(offsetX, offsetY, offsetZ))
				val newKey = toBlockKey(newVec.x, newVec.y, newVec.z)

				// Center of the grid is already visited
				if (currentKey == newKey) continue

				val newBlock = starship.world.getBlockAt(newVec.x, newVec.y, newVec.z)

				// Already visited
				if (visitSet.contains(newKey)) continue

				if (!canDetect(newBlock)) {
					continue
				}

				visitSet.add(newKey)
				visitQueue.addLast(newVec)

				Tasks.asyncDelay(iterations) { debugAudience.highlightBlock(Vec3i(newVec.x, newVec.y, newVec.z), 30L) }
			}
		}

		captiveSubsystems = LinkedList(foundSubsystems)
		blocks = foundBlocks.toLongArray()
	}

	private fun canDetect(block: Block): Boolean {
		// Detect all blocks above the turret base
		return block.y > pos.y
	}

	override fun onMovement(movement: StarshipMovement, success: Boolean) {
		if (!success) return
		// Offset the blocks when the ship moves
		blocks = LongArray(blocks.size) { movement.displaceKey(blocks[it]) }
	}

	fun rotate(newFace: BlockFace): Boolean {
		if (starship.isTeleporting) return false
		val oldFace = face

		val i = when (newFace) {
			oldFace -> return true
			oldFace.rightFace -> 1
			oldFace.oppositeFace -> 2
			oldFace.leftFace -> 3
			else -> error("Failed to calculate rotation iteration count from $oldFace to $newFace")
		}

		val theta: Double = 90.0 * i

		try {
			moveBlocks(theta)
			face = newFace
		} catch (e: StarshipMovementException) {
			return false
		}

		return true
	}

	private fun moveBlocks(thetaDegrees: Double): Boolean {
		if (starship.isMoving) return false

		val cosTheta: Double = cos(Math.toRadians(thetaDegrees))
		val sinTheta: Double = sin(Math.toRadians(thetaDegrees))

		val newPositionArray = LongArray(blocks.size)
		val newPositionSet = LongOpenHashSet(blocks.size)

		val nmsRotation =  when (thetaDegrees % 360.0) {
			in -45.0..< 45.0 -> Rotation.NONE
			in 45.0..< 135.0 -> Rotation.CLOCKWISE_90
			in 135.0..< 225.0 -> Rotation.CLOCKWISE_180
			in 225.0..< 315.0 -> Rotation.COUNTERCLOCKWISE_90
			in 315.0.. 360.0 -> Rotation.NONE
			else -> throw IllegalArgumentException("something mod 360 returned more than 360?")
		}

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
			executionCheck = {  !starship.isMoving },
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
			starship.calculateHitbox()

			totalRotation += thetaDegrees
			rotateCapturedSubsystems(sinTheta, cosTheta, nmsRotation)
		}

		return true
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

	/** Total rotation that the turret has performed */
	private var totalRotation = 0.0

	override fun onDestroy() {
		// Rotate back to home position
		moveBlocks(360 - (totalRotation % 360))
	}
}
