package net.horizonsend.ion.server.features.starship

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import it.unimi.dsi.fastutil.longs.LongIterator
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.horizonsend.ion.common.utils.miscellaneous.d
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.space.SpaceWorlds
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarshipMechanics
import net.horizonsend.ion.server.features.starship.event.StarshipExplodeEvent
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.blockKey
import net.horizonsend.ion.server.miscellaneous.utils.blockKeyX
import net.horizonsend.ion.server.miscellaneous.utils.blockKeyY
import net.horizonsend.ion.server.miscellaneous.utils.blockKeyZ
import net.horizonsend.ion.server.miscellaneous.utils.blockplacement.BlockPlacement
import net.horizonsend.ion.server.miscellaneous.utils.distanceSquared
import net.horizonsend.ion.server.miscellaneous.utils.nms
import net.minecraft.world.level.block.state.BlockState
import org.bukkit.Material
import org.bukkit.World
import java.util.LinkedList
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.collections.set
import kotlin.math.sqrt
import kotlin.random.Random

object StarshipDestruction {
	const val MAX_SAFE_HULL_INTEGRITY = 0.8

	fun vanish(starship: ActiveStarship) {
		if (starship.isExploding) {
			return
		}

		starship.isExploding = true

		if (starship is ActiveControlledStarship) {
			DeactivatedPlayerStarships.deactivateAsync(starship) {
				DeactivatedPlayerStarships.destroyAsync(starship.data) {
					vanishShip(starship)
				}
			}
		} else {
			vanishShip(starship)
		}
	}

	private fun vanishShip(starship: ActiveStarship) {
		val air = Material.AIR.createBlockData().nms
		val queue = Long2ObjectOpenHashMap<BlockState>(starship.initialBlockCount)
		starship.blocks.associateWithTo(queue) { air }
		BlockPlacement.placeImmediate(starship.world, queue)
	}

	fun destroy(starship: ActiveStarship) {
		if (starship.isExploding) {
			return
		}
		if (!StarshipExplodeEvent(starship).callEvent()) {
			return
		}

		starship.isExploding = true

		if (starship is ActiveControlledStarship) {
			DeactivatedPlayerStarships.deactivateAsync(starship) {
				DeactivatedPlayerStarships.destroyAsync(starship.data) {
					destroyShip(starship)
				}
			}
		} else {
			destroyShip(starship)
		}
	}

	private fun destroyShip(starship: ActiveStarship) {
		val world = starship.world
		val blocks = starship.blocks
		if (SpaceWorlds.contains(world)) {
			zeroGSink(world, blocks)
		} else {
			sink(world, blocks)
		}
	}

	private val air = Material.AIR.createBlockData()

	private val limitPerTick = TimeUnit.MILLISECONDS.toNanos(10)

	private fun sink(world: World, blocks: LongOpenHashSet) {
		val sinking = LongOpenHashSet(blocks)

		val newSinking = LinkedBlockingQueue<Long>()

		val placements = Long2ObjectOpenHashMap<BlockState>(sinking.size)

		var iterator = sinking.iterator()

		val obstructedLocs = LongOpenHashSet()

		Tasks.bukkitRunnable {
			if (!processVerticalSinkQueue(iterator, obstructedLocs, world, newSinking, sinking, placements)) {
				return@bukkitRunnable
			}

			placements.clear()

			removeBlocksAroundObstructed(newSinking, obstructedLocs)

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
		}.runTaskTimerAsynchronously(IonServer, 20L, 20L)
	}

	private fun zeroGSink(world: World, blocks: LongOpenHashSet) {
		val sinking = LongOpenHashSet(blocks)

		val newSinking = LinkedBlockingQueue<Long>()

		val placements = Long2ObjectOpenHashMap<BlockState>(sinking.size)

		var iterator = sinking.iterator()

		val obstructedLocs = LongOpenHashSet()

		val random = Random(blocks.hashCode())

		val randomVelocity = Triple(
			random.nextDouble(-2.0, 2.0),
			random.nextDouble(-2.0, 2.0),
			random.nextDouble(-2.0, 2.0)
		)

		val maxIteration = sqrt(blocks.size.toDouble())
		var iteration = 0

		Tasks.bukkitRunnable {
			iteration++

			if (iteration > maxIteration) {
				cancel()
				Tasks.sync {
					explode(world, blocks)
				}
			}

			if (!processHorizontalSinkQueue(iterator, obstructedLocs, world, newSinking, randomVelocity, sinking, placements)) {
				return@bukkitRunnable
			}

			placements.clear()

			removeBlocksAroundObstructed(newSinking, obstructedLocs)

			val randomBlock = blocks.random()
			val x = blockKeyX(randomBlock).toDouble()
			val y = blockKeyY(randomBlock).toDouble()
			val z = blockKeyZ(randomBlock).toDouble()

			Tasks.sync { world.createExplosion(x, y, z, 8.0f) }

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
		}.runTaskTimerAsynchronously(IonServer, 20L, 20L)
	}

	private fun processVerticalSinkQueue(
		iterator: LongIterator,
		obstructedLocations: LongOpenHashSet,
		world: World,
		newSinkingBlocks: LinkedBlockingQueue<Long>,
		sinkingBlocks: LongOpenHashSet,
		placements: Long2ObjectOpenHashMap<BlockState>
	): Boolean = Tasks.getSyncBlocking {
		val start = System.nanoTime()

		while (iterator.hasNext()) {
			if (System.nanoTime() - start > limitPerTick) {
				return@getSyncBlocking false
			}

			val key = iterator.nextLong()
			val x = blockKeyX(key)
			val y = blockKeyY(key)
			val z = blockKeyZ(key)
			val newY = y - 1
			if (newY < 1) {
				obstructedLocations.add(key)
				continue
			}
			val block = world.getBlockAtKey(key)
			val blockData = block.blockData
			val belowKey = blockKey(x, newY, z)
			newSinkingBlocks.add(belowKey)
			if (!sinkingBlocks.contains(belowKey)) {
				val below = world.getBlockAtKey(belowKey)
				val belowData = below.blockData
				if (belowData.nms.material.isLiquid) {
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

	private fun processHorizontalSinkQueue(
		iterator: LongIterator,
		obstructedLocations: LongOpenHashSet,
		world: World,
		newSinkingBlocks: LinkedBlockingQueue<Long>,
		velocity: Triple<Double, Double, Double>,
		sinkingBlocks: LongOpenHashSet,
		placements: Long2ObjectOpenHashMap<BlockState>
	): Boolean = Tasks.getSyncBlocking {
		val start = System.nanoTime()

		while (iterator.hasNext()) {
			if (System.nanoTime() - start > limitPerTick) return@getSyncBlocking false

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

			val block = world.getBlockAtKey(key)
			val blockData = block.blockData
			val belowKey = blockKey(newX, newY, newZ)

			newSinkingBlocks.add(belowKey)

			if (!sinkingBlocks.contains(belowKey)) {
				val below = world.getBlockAtKey(belowKey)
				val belowData = below.blockData

				if (belowData.nms.material.isLiquid) {
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
				ActiveStarshipMechanics.withBlockExplosionDamageAllowed {
					world.createExplosion(x, y, z, 6.0f)
				}
			}
		}

		val finalDelay = ticksBetweenExplosions * (i / blockInterval) + 10

		Tasks.syncDelayTask(finalDelay) {
			for (block in queue) {
				val x = blockKeyX(block).toDouble()
				val y = blockKeyY(block).toDouble()
				val z = blockKeyZ(block).toDouble()
				ActiveStarshipMechanics.withBlockExplosionDamageAllowed {
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
}
