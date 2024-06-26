package net.horizonsend.ion.server.features.starship.destruction

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import it.unimi.dsi.fastutil.longs.LongIterator
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.horizonsend.ion.common.utils.miscellaneous.d
import net.horizonsend.ion.server.features.space.SpaceWorlds
import net.horizonsend.ion.server.features.starship.Hangars
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarshipMechanics
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.blockKey
import net.horizonsend.ion.server.miscellaneous.utils.blockKeyX
import net.horizonsend.ion.server.miscellaneous.utils.blockKeyY
import net.horizonsend.ion.server.miscellaneous.utils.blockKeyZ
import net.horizonsend.ion.server.miscellaneous.utils.blockplacement.BlockPlacement
import net.horizonsend.ion.server.miscellaneous.utils.distanceSquared
import net.horizonsend.ion.server.miscellaneous.utils.isBlockLoaded
import net.horizonsend.ion.server.miscellaneous.utils.nms
import net.minecraft.world.level.block.state.BlockState
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.util.Vector
import java.util.LinkedList
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Makes the ship fall / drift while slowly exploding
 **/
open class StandardSinkProvider(starship: ActiveStarship) : SinkProvider(starship) {
	private val blocks = starship.blocks
	private val velocity: Vector = Vector()

	private val sinking = LongOpenHashSet(blocks)
	private val newSinking = LinkedBlockingQueue<Long>()

	private val placements = Long2ObjectOpenHashMap<BlockState>(sinking.size)
	private var iterator = sinking.iterator()

	private val obstructedLocs = LongOpenHashSet()

	private val maxIteration = sqrt(blocks.size.toDouble())

	private var iteration = 0
	private val world = starship.world

	override fun setup() {
		val world = starship.world
		val blocks = starship.blocks

		if (SpaceWorlds.contains(world)) {
			val random = Random(blocks.hashCode())

			velocity.setX(random.nextDouble(-1.0, 1.0))
			velocity.setY(random.nextDouble(-1.0, 1.0))
			velocity.setZ(random.nextDouble(-1.0, 1.0))
		} else {
			velocity.setY(-1.0)
		}
	}

	override fun tick() {
		iteration++

		if (iteration > maxIteration) {
			cancel()
			Tasks.sync {
				explode(starship.world, blocks)
			}
		}

		if (!processSinkQueue(iterator, obstructedLocs, world, newSinking, sinking, placements, Vec3i(velocity))) {
			return
		}

		placements.clear()

		removeBlocksAroundObstructed(newSinking, obstructedLocs)

		val randomBlock = blocks.random()
		val x = blockKeyX(randomBlock).toDouble()
		val y = blockKeyY(randomBlock).toDouble()
		val z = blockKeyZ(randomBlock).toDouble()

		if (isBlockLoaded(world, x, y, z)) {
			Tasks.sync { world.createExplosion(x, y, z, 8.0f) }
		}

		sinking.clear()
		sinking.addAll(newSinking)
		iterator = sinking.iterator()
		blocks.removeAll(placements.keys)
		blocks.addAll(newSinking)

		if (newSinking.isEmpty()) {
			cancel()
			Tasks.sync {
				explode(world, blocks)
			}
		} else {
			newSinking.clear()
		}
	}

	private fun processSinkQueue(
		iterator: LongIterator,
		obstructedLocations: LongOpenHashSet,
		world: World,
		newSinkingBlocks: LinkedBlockingQueue<Long>,
		sinkingBlocks: LongOpenHashSet,
		placements: Long2ObjectOpenHashMap<BlockState>,
		velocity: Vec3i = Vec3i(0, -1, 0)
	) = Tasks.getSyncBlocking {
		val start = System.nanoTime()

		while (iterator.hasNext()) {
			if (System.nanoTime() - start > limitPerTick) {
				return@getSyncBlocking false
			}

			val key = iterator.nextLong()

			val x = blockKeyX(key)
			val y = blockKeyY(key)
			val z = blockKeyZ(key)

			val (xOffset, yOffset, zOffset) = velocity

			val newX = x + xOffset
			val newY = y + yOffset
			val newZ = z + zOffset

			if (newY < world.minHeight || newY > world.maxHeight) {
				obstructedLocations.add(key)
				continue
			}

			if (!world.worldBorder.isInside(Location(world, x.toDouble(), y.toDouble(), z.toDouble()))) {
				obstructedLocations.add(key)
				continue
			}

			// Check here, also when exploding
			if (!isBlockLoaded(world, newX, newY, newZ)) {
				obstructedLocations.add(key)
				continue
			}

			val block = world.getBlockAtKey(key)
			val blockData = block.blockData
			val belowKey = blockKey(newX, newY, newZ)

			newSinkingBlocks.add(belowKey)

			if (!sinkingBlocks.contains(belowKey)) {
				val below = world.getBlockAtKey(belowKey)
				val belowData = below.blockData

				if (belowData.nms.liquid()) {
					Hangars.dissipateBlock(world, belowKey)
				} else if (!belowData.material.isAir) {
					obstructedLocations.add(key)
					continue
				}
			}

			placements.putIfAbsent(key, air.nms)
			placements[belowKey] = blockData.nms
		}

		BlockPlacement.placeImmediate(world, placements)
		return@getSyncBlocking true
	}

	private fun removeBlocksAroundObstructed(newSinking: LinkedBlockingQueue<Long>, obstructedLocs: LongOpenHashSet) {
		newSinking.removeIf { a ->
			val aX = blockKeyX(a).d()
			val aY = blockKeyY(a).d()
			val aZ = blockKeyZ(a).d()
			obstructedLocs.any { b ->
				val bX = blockKeyX(b).d()
				val bY = blockKeyY(b).d()
				val bZ = blockKeyZ(b).d()
				distanceSquared(aX, aY, aZ, bX, bY, bZ) < 4
			}
		}
	}

	private fun explode(world: World, blocks: LongOpenHashSet) {
		var i = 0
		val blockInterval = 500
		val queueInterval = 200
		val ticksBetweenExplosions = 4L

		val queue = LinkedList<Long>()

		for (block in blocks.iterator()) {
			i++

			if (i % queueInterval == 0) {
				queue.add(block)
			}

			if (i % blockInterval != 0) {
				continue
			}

			val x = blockKeyX(block).toDouble()
			val y = blockKeyY(block).toDouble()
			val z = blockKeyZ(block).toDouble()

			val delay = ticksBetweenExplosions * (i / blockInterval)
			Tasks.syncDelayTask(delay) {
				if (isBlockLoaded(world, x, y, z)) {
					ActiveStarshipMechanics.withBlockExplosionDamageAllowed {
						world.createExplosion(x, y, z, 6.0f)
					}
				}
			}
		}

		val finalDelay = ticksBetweenExplosions * (i / blockInterval) + 10

		Tasks.syncDelayTask(finalDelay) {
			for (block in queue) {
				val x = blockKeyX(block).toDouble()
				val y = blockKeyY(block).toDouble()
				val z = blockKeyZ(block).toDouble()

				if (isBlockLoaded(world, x, y, z)) ActiveStarshipMechanics.withBlockExplosionDamageAllowed {
					world.createExplosion(x, y, z, 8.0f)
				}
			}
		}

		if (world.name == "SpaceArena") {
			val air = Material.AIR.createBlockData()

			Tasks.syncDelayTask(finalDelay) {
				for (key in blocks.iterator()) {
					world.getBlockAtKey(key).setBlockData(air, false)
				}
			}
		}
	}

	companion object {
		private val air = Material.AIR.createBlockData()
		private val limitPerTick = TimeUnit.MILLISECONDS.toNanos(10)
	}
}
