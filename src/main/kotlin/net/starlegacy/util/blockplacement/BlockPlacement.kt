package net.starlegacy.util.blockplacement

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import net.minecraft.world.level.block.state.BlockState
import net.starlegacy.util.Tasks
import org.bukkit.World

object BlockPlacement {
	private val raw = BlockPlacementRaw()

	init {
		Tasks.syncRepeat(1, 1) { raw.flush(null) }
	}

	fun queue(world: World, queue: Long2ObjectOpenHashMap<BlockState>) = raw.queue(world, queue)

	fun flush(onComplete: ((World) -> Unit)? = null): Unit = raw.flush(onComplete)

	// can be called async
	fun placeQueueEfficiently(
		world: World,
		queue: Long2ObjectOpenHashMap<BlockState>,
		onComplete: ((World) -> Unit)? = null
	) {
		val worldQueue = Long2ObjectOpenHashMap<Array<Array<Array<BlockState>>>>()
		raw.addToWorldQueue(queue, worldQueue, world)
		Tasks.sync {
			raw.placeWorldQueue(world, worldQueue, onComplete, false)
		}
	}

	fun placeImmediate(
		world: World,
		queue: Long2ObjectOpenHashMap<BlockState>,
		onComplete: ((World) -> Unit)? = null
	) {
		val worldQueue = Long2ObjectOpenHashMap<Array<Array<Array<BlockState>>>>()
		raw.addToWorldQueue(queue, worldQueue, world)
		raw.placeWorldQueue(world, worldQueue, onComplete, true)
	}
}
