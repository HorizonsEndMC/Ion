package net.horizonsend.ion.server.features.starship.subsystem.misc.tractor

import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.command.admin.debug
import net.horizonsend.ion.server.features.multiblock.MultiblockEntities
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.starship.Mass
import net.horizonsend.ion.server.features.starship.movement.StarshipMovementException
import net.horizonsend.ion.server.features.starship.movement.TransformationAccessor
import net.horizonsend.ion.server.gui.invui.misc.util.input.validator.ValidatorResult
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.blockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.blockKeyX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.blockKeyY
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.blockKeyZ
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getY
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.getBlockIfLoaded
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.World
import org.bukkit.entity.Player
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import kotlin.math.cbrt

class TowedBlocks private constructor(
	world: World,
    val subsystem: TractorBeamSubsystem,
    val multiblockEntities: ObjectOpenHashSet<MultiblockEntity>,
    blocks: LongArray,
    minPoint: Vec3i,
    maxPoint: Vec3i,
    val mass: Double
) {
	var world: World = world; private set
	var blocks: LongArray = blocks; private set
	var blockSet = LongOpenHashSet(blocks); private set
	var minPoint: Vec3i = minPoint; private set
	var maxPoint: Vec3i = maxPoint; private set

	var movementFuture: Future<Boolean>? = null

	val centerPoint: Vec3i? get() = Vec3i((minPoint.x + maxPoint.x) / 2, (minPoint.y + maxPoint.y) / 2, (minPoint.z + maxPoint.z) / 2)

	val rotationTime get() = TimeUnit.MILLISECONDS.toNanos(50L + blocks.size / 30L)
	val manualMoveCooldown get() = (cbrt(blocks.size.toDouble()) * 40).toLong()

	fun contains(position: Vec3i): Boolean {
		return blockSet.contains(position.toBlockKey())
	}

	fun contains(x: Int, y: Int, z: Int): Boolean {
		return blockSet.contains(blockKey(x, y, z))
	}

	fun move(oldWorld: World, transformationAccessor: TransformationAccessor) {
		var lastTicked = System.currentTimeMillis()

		if (movementFuture?.isDone == false) {
			return
		}

		val future = CompletableFuture<Boolean>()
		movementFuture = future

		Tasks.async {
			try {
				subsystem.starship.debug("Moving ${blocks.size} blocks")

				transformationAccessor.execute(
					positions = blocks,
					world1 = oldWorld,
					executionCheck = { true }
				) { newBlocks ->
					blocks = newBlocks
					blockSet = LongOpenHashSet(newBlocks)

					recalculateMinMax()
				}

				Tasks.syncBlocking {
					displaceMultiblockEntities(transformationAccessor)
				}

				val now = System.currentTimeMillis()
				subsystem.starship.debug("Movement took ${(now - lastTicked) / 1000.0}s")
				lastTicked = now
				transformationAccessor.newWorld?.let { world = it }

				future.complete(true)
			} catch (e: StarshipMovementException) {
				subsystem.starship.sendMessage(ofChildren(Component.text("Towed Load Blocked! ", NamedTextColor.RED), e.formatMessage()))
				future.complete(false)
			}
		}
	}

	fun displaceMultiblockEntities(transformationAccessor: TransformationAccessor) {
		for (entity in multiblockEntities) {
			val localVec3i = entity.manager.getTransportManager().getLocalCoordinate(transformationAccessor.displaceVec3i(entity.globalVec3i))

			entity.localOffsetX = localVec3i.x
			entity.localOffsetY = localVec3i.y
			entity.localOffsetZ = localVec3i.z

			entity.displace(transformationAccessor)
		}
	}

	fun recalculateMinMax() {
		if (blocks.isEmpty()) return

		val start = blocks.iterator().next()
		var minX = blockKeyX(start)
		var minY = blockKeyY(start)
		var minZ = blockKeyZ(start)

		var maxX = minX
		var maxY = minY
		var maxZ = minZ

		for (key in blocks) {
			val x = blockKeyX(key)
			val y = blockKeyY(key)
			val z = blockKeyZ(key)

			if (x < minX) minX = x
			if (x > maxX) maxX = x

			if (y < minY) minY = y
			if (y > maxY) maxY = y

			if (z < minZ) minZ = z
			if (z > maxZ) maxZ = z
		}

		minPoint = Vec3i(minX, minY, minZ)
		maxPoint = Vec3i(maxX, maxY, maxZ)
	}

	companion object {
		fun build(user: Player, origin: Vec3i, subsystem: TractorBeamSubsystem): ValidatorResult<TowedBlocks> {
			val originKey = toBlockKey(origin)
			val world = subsystem.starship.world

			val visited = LongOpenHashSet.of(originKey)
			val visitQueue = ArrayDeque<Long>(listOf(originKey))

			val foundMultiblockEntities = ObjectOpenHashSet<MultiblockEntity>()

			val foundBlocks = LongOpenHashSet()

			var minX: Int = Int.MAX_VALUE
			var minY: Int = Int.MAX_VALUE
			var minZ: Int = Int.MAX_VALUE

			var maxX: Int = Int.MIN_VALUE
			var maxY: Int = Int.MIN_VALUE
			var maxZ: Int = Int.MIN_VALUE

			val towLimit = subsystem.getTowLimit()

			var totalMass = 0.0

			while (visitQueue.isNotEmpty()) {
				val current = visitQueue.removeFirst()
				val x = getX(current)
				val y = getY(current)
				val z = getZ(current)

				if (y !in world.minHeight..<world.maxHeight) continue

				val block = getBlockIfLoaded(world, x, y, z)

				if (block == null) {
					return ValidatorResult.FailureResult(listOf(Component.text("That structure goes beyond loaded chunks!", NamedTextColor.RED)))
				}

				if (!subsystem.verifyBlock(user, block)) continue

				foundBlocks.add(blockKey(x, y, z))

				val mass = Mass[block.type]
				totalMass += mass

				val multiblockEntity = MultiblockEntities.getMultiblockEntity(block)
				if (multiblockEntity != null) {
					foundMultiblockEntities.add(multiblockEntity)
				}

				if (foundBlocks.size > towLimit) {
					val message = template(Component.text("That structure is too large to move! You may only tow {0} blocks!", NamedTextColor.RED), towLimit)

					return ValidatorResult.FailureResult(listOf(message))
				}

				if (minX > x) minX = x
				if (minY > y) minY = y
				if (minZ > z) minZ = z
				if (maxX < x) maxX = x
				if (maxY < y) maxY = y
				if (maxZ < z) maxZ = z

				// Detect adjacent blocks
				for (offsetX in -1..1) for (offsetY in -1..1) for (offsetZ in -1..1) {
					val adjacentX = offsetX + x
					val adjacentY = offsetY + y
					val adjacentZ = offsetZ + z

					// Ensure it's a valid Y-level before adding it to the queue
					if (adjacentY < 0 || adjacentY > world.maxHeight) {
						continue
					}

					val adjacent = toBlockKey(adjacentX, adjacentY, adjacentZ)
					if (visited.add(adjacent)) {
						visitQueue.addFirst(adjacent)
					}
				}
			}

			if (foundBlocks.isEmpty()) return ValidatorResult.FailureResult(listOf(Component.text("No blocks found.")))

			return ValidatorResult.ValidatorSuccessSingleEntry(TowedBlocks(
				world = subsystem.starship.world,
                subsystem = subsystem,
                multiblockEntities = foundMultiblockEntities,
                blocks = foundBlocks.toLongArray(),
                minPoint = Vec3i(minX, minY, minZ),
                maxPoint = Vec3i(maxX, maxY, maxZ),
                mass = totalMass
			))
		}
	}
}
