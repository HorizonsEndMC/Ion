package net.starlegacy.feature.starship

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import it.unimi.dsi.fastutil.longs.LongIterator
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.horizonsend.ion.server.IonServer.Companion.Ion
import net.minecraft.world.level.block.state.BlockState
import net.starlegacy.feature.space.SpaceWorlds
import net.starlegacy.feature.starship.active.ActivePlayerStarship
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.active.ActiveStarshipMechanics
import net.starlegacy.feature.starship.event.StarshipExplodeEvent
import net.starlegacy.util.Tasks
import net.starlegacy.util.blockKey
import net.starlegacy.util.blockKeyX
import net.starlegacy.util.blockKeyY
import net.starlegacy.util.blockKeyZ
import net.starlegacy.util.blockplacement.BlockPlacement
import net.starlegacy.util.d
import net.starlegacy.util.distanceSquared
import net.starlegacy.util.nms
import org.bukkit.Material
import org.bukkit.World
import java.util.LinkedList
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.collections.set

object StarshipDestruction {
	const val MAX_SAFE_HULL_INTEGRITY = 0.8

	fun vanish(starship: ActiveStarship) {
		if (starship.isExploding) {
			return
		}

		starship.isExploding = true

		if (starship is ActivePlayerStarship) {
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

		if (starship is ActivePlayerStarship) {
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
			explode(world, blocks)
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
			if (!processQueue(iterator, obstructedLocs, world, newSinking, sinking, placements)) {
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
		}.runTaskTimerAsynchronously(Ion, 20L, 20L)
	}

	private fun processQueue(
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
