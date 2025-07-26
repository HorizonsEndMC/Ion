package net.horizonsend.ion.server.features.starship.subsystem.misc.tug

import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.starship.movement.StarshipMovementException
import net.horizonsend.ion.server.features.starship.movement.TransformationAccessor
import net.horizonsend.ion.server.features.starship.subsystem.misc.tug.TugSubsystem.Companion.MAX_ASTEROID_SIZE
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
import org.bukkit.entity.Player
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

class TowedBlocks private constructor (
	val subsystem: TugSubsystem,
	blocks: LongArray,
	minPoint: Vec3i,
	maxPoint: Vec3i,
	val weight: Double
) {
	var blocks: LongArray = blocks; private set
	var minPoint: Vec3i = minPoint; private set
	var maxPoint: Vec3i = maxPoint; private set

	var movementFuture: Future<Boolean>? = null

	val centerPoint: Vec3i? get() = Vec3i((minPoint.x + maxPoint.x) / 2, (minPoint.y + maxPoint.y) / 2, (minPoint.z + maxPoint.z) / 2)

	fun move(transformationAccessor: TransformationAccessor) {
		var lastTicked = System.currentTimeMillis()

		if (movementFuture?.isDone == false) {
			return
		}

		val future = CompletableFuture<Boolean>()
		movementFuture = future

		Tasks.async {
			try {
				subsystem.starship.information("Moving ${blocks.size} blocks")

				transformationAccessor.execute(
					positions = blocks,
					world1 = subsystem.starship.world,
					executionCheck = { true }
				) { newBlocks ->
					blocks = newBlocks

					recalculateMinMax()
				}

				val now = System.currentTimeMillis()
				subsystem.starship.information("Movement took ${(now - lastTicked) / 1000.0}s")
				lastTicked = now
				future.complete(true)
			} catch (e: StarshipMovementException) {
				subsystem.starship.sendMessage(ofChildren(Component.text("Towed Load Blocked! ", NamedTextColor.RED), e.formatMessage()))
				future.complete(false)
			}
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
		fun build(user: Player, origin: Vec3i, subsystem: TugSubsystem): ValidatorResult<TowedBlocks> {
			val originKey = toBlockKey(origin)
			val world = subsystem.starship.world

			val visited = LongOpenHashSet.of(originKey)
			val visitQueue = ArrayDeque<Long>(listOf(originKey))

			val foundBlocks = LongOpenHashSet()

			var minX: Int = Int.MAX_VALUE
			var minY: Int = Int.MAX_VALUE
			var minZ: Int = Int.MAX_VALUE

			var maxX: Int = Int.MIN_VALUE
			var maxY: Int = Int.MIN_VALUE
			var maxZ: Int = Int.MIN_VALUE

			while (visitQueue.isNotEmpty()) {
				val current = visitQueue.removeFirst()
				val x = getX(current)
				val y = getY(current)
				val z = getZ(current)

				val block = getBlockIfLoaded(world, x, y, z)

				if (block == null) {
					return ValidatorResult.FailureResult(listOf(Component.text("That structure goes beyond loaded chunks!")))
				}

				if (!subsystem.verifyBlock(user, block)) continue

				foundBlocks.add(blockKey(x, y, z))

				if (foundBlocks.size > MAX_ASTEROID_SIZE) {
					return ValidatorResult.FailureResult(listOf(Component.text("That structure is too large to move!")))
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
				subsystem,
				foundBlocks.toLongArray(),
				Vec3i(minX, minY, minZ),
				Vec3i(maxX, maxY, maxZ),
				0.0
			))
		}
	}
}
