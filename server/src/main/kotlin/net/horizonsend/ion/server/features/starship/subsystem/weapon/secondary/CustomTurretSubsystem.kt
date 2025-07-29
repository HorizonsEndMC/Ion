package net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.highlightBlock
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.turret.CustomTurretBaseMultiblock
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.active.ActiveStarshipFactory
import net.horizonsend.ion.server.features.starship.movement.StarshipMovementException
import net.horizonsend.ion.server.features.starship.movement.TransformationAccessor
import net.horizonsend.ion.server.features.starship.subsystem.DirectionalSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.StarshipSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.TurretWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.WeaponSubsystem
import net.horizonsend.ion.server.features.transport.NewTransport
import net.horizonsend.ion.server.miscellaneous.utils.*
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.*
import net.kyori.adventure.text.Component
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Directional
import org.bukkit.util.Vector
import java.util.*

class CustomTurretSubsystem(starship: Starship, pos: Vec3i, override var face: BlockFace, val multiblock: CustomTurretBaseMultiblock) : WeaponSubsystem(starship, pos), DirectionalSubsystem {
	init {
		val furnacePos = pos.plus(multiblock.furnaceOffset)
	    val furnaceBlock = starship.world.getBlockAtKey(furnacePos.toBlockKey()).blockData as? Directional
		if (furnaceBlock != null) face = furnaceBlock.facing
	}

	override val balancing: StarshipWeapons.StarshipWeapon = StarshipWeapons.StarshipWeapon(
		range = 0.0,
		speed = 0.0,
		areaShieldDamageMultiplier = 0.0,
		starshipShieldDamageMultiplier = 0.0,
		particleThickness = 0.0,
		explosionPower = 0.0f,
		volume = 0,
		pitch = 0.0f,
		soundName = "",
		powerUsage = 0,
		length = 0 ,
		angleRadiansHorizontal = 0.0,
		angleRadiansVertical = 0.0,
		convergeDistance = 0.0,
		extraDistance = 0,
		fireCooldownMillis = 0,
		boostChargeSeconds = 0,
		aimDistance = 0,
		applyCooldownToAll = false
	)

	override val powerUsage: Int = 0
	override fun getName(): Component = Component.text("Custom Turret")

	override fun canFire(dir: Vector, target: Vector): Boolean = true
	override fun getAdjustedDir(dir: Vector, target: Vector): Vector = dir

	companion object {
		val disallowedSubsystems = setOf(CustomTurretSubsystem::class, TurretWeaponSubsystem::class)
	}

	override fun isIntact(): Boolean {
		// Only check the base
		val block = getBlockIfLoaded(starship.world, pos.x, pos.y, pos.z) ?: return false
		return multiblock.shape.checkRequirements(block, face, loadChunks = false, particles = false)
	}

	var blocks = LongArray(0); private set
	private var captiveSubsystems = LinkedList<StarshipSubsystem>()
	private var capturedMultiblockEntities = LinkedList<MultiblockEntity>()

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
		val foundBlocks = LongOpenHashSet.of(pos.plus(multiblock.furnaceOffset).toBlockKey())
		val foundSubsystems = ObjectOpenHashSet<StarshipSubsystem>()
		val foundMultiblocks = ObjectOpenHashSet<MultiblockEntity>()

		// Queue of blocks that need to be visited
		val visitQueue = ArrayDeque<Vec3i>()
		// Set of all blocks that have been visited
		val visitSet = LongOpenHashSet()

		// Jump to start with the origin
		visitQueue.add(Vec3i(pos.x, pos.y, pos.z).plus(multiblock.detectionOrigin))

		var iterations = 0L

		// Perform a flood fill to find turret blocks
		while (visitQueue.isNotEmpty()) {
			iterations++
			val currentVec3i = visitQueue.removeFirst()
			val currentKey = toBlockKey(currentVec3i.x, currentVec3i.y, currentVec3i.z)
			val legacyKey = blockKey(currentVec3i.x, currentVec3i.y, currentVec3i.z)

			if (!starshipBlocks.contains(legacyKey)) continue

			// Block can be a part of the turret
			foundBlocks.add(legacyKey)

			val subsystem = subsystemsByPos[currentKey]
			subsystem?.let {
				if (disallowedSubsystems.contains(it::class)) throw ActiveStarshipFactory.StarshipActivationException("${subsystem.javaClass.simpleName}s cannot be part of custom turrets!")
				foundSubsystems.add(it)
			}
			starship.multiblockManager.getFromGlobalKey(currentKey)?.let {
				foundMultiblocks.add(it)
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
		capturedMultiblockEntities = LinkedList(foundMultiblocks)
		blocks = foundBlocks.toLongArray()
	}

	private fun canDetect(block: Block): Boolean {
		// Detect all blocks above the turret base
		return block.y > multiblock.detectionOrigin.y + pos.y
	}

	override fun onMovement(movement: TransformationAccessor, success: Boolean) {
		if (!success) return
		// Offset the blocks when the ship moves
		blocks = LongArray(blocks.size) { movement.displaceLegacyKey(blocks[it]) }
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

		return if (moveBlocks(theta)) {
			face = newFace
			true
		}
		else false
	}

	private fun moveBlocks(thetaDegrees: Double): Boolean {
		if (starship.isMoving) return false

		try {
			val oldPositions = blocks

			val transformationAccessor = TransformationAccessor.RotationTransformation(null, thetaDegrees, this::pos)
			transformationAccessor.execute(blocks, starship.world, { !starship.isMoving }) { newPositionArray ->
				blocks = newPositionArray

				starship.blocks.removeAll(LongOpenHashSet(blocks))
				starship.blocks.addAll(LongOpenHashSet(newPositionArray))

				starship.calculateHitbox()

				totalRotation += thetaDegrees
				rotateCapturedSubsystems(transformationAccessor)

				Tasks.async {
					for (key in oldPositions.toModernBlockKey().union(LongOpenHashSet(newPositionArray.toModernBlockKey()))) {
						NewTransport.invalidateCache(starship.world, getX(key), getY(key), getZ(key))
					}
				}
			}
		} catch (e: StarshipMovementException) {
			return false
		}

		return true
	}

	private fun rotateCapturedSubsystems(translation: TransformationAccessor.RotationTransformation) {
		for (subsystem in captiveSubsystems) {
			val oldX = subsystem.pos.x
			val oldZ = subsystem.pos.z

			subsystem.pos = Vec3i(
				translation.displaceX(oldX, oldZ),
				subsystem.pos.y,
				translation.displaceZ(oldZ, oldX)
			)

			subsystem.onMovement(translation, true)

			if (subsystem is DirectionalSubsystem) {
				subsystem.face = rotateBlockFace(subsystem.face, translation.nmsRotation)
			}
		}

		for (entity in capturedMultiblockEntities) {
			val localVec3i = starship.getLocalCoordinate(translation.displaceVec3i(entity.globalVec3i))

			entity.localOffsetX = localVec3i.x
			entity.localOffsetY = localVec3i.y
			entity.localOffsetZ = localVec3i.z

			entity.displace(translation)
		}
	}

	/** Total rotation that the turret has performed */
	private var totalRotation = 0.0

	override fun onDestroy() {
		// Rotate back to home position
		moveBlocks(360 - (totalRotation % 360))
	}
}
