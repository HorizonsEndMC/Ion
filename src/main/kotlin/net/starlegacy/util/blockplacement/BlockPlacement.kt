package net.starlegacy.util.blockplacement

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import net.minecraft.server.v1_16_R3.IBlockData
import net.starlegacy.util.NMSBlockData
import net.starlegacy.util.Tasks
import org.bukkit.World

object BlockPlacement {
    private val raw = BlockPlacementRaw()

    init {
        Tasks.syncRepeat(1, 1) { raw.flush(null) }
    }

    fun queue(world: World, queue: Long2ObjectOpenHashMap<NMSBlockData>) = raw.queue(world, queue)

    fun flush(onComplete: ((World) -> Unit)? = null): Unit = raw.flush(onComplete)

    // can be called async
    fun placeQueueEfficiently(
        world: World,
        queue: Long2ObjectOpenHashMap<NMSBlockData>,
        onComplete: ((World) -> Unit)? = null
    ) {
        val worldQueue = Long2ObjectOpenHashMap<Array<Array<Array<IBlockData>>>>()
        raw.addToWorldQueue(queue, worldQueue)
        Tasks.sync {
            raw.placeWorldQueue(world, worldQueue, onComplete, false)
        }
    }

    fun placeImmediate(
        world: World,
        queue: Long2ObjectOpenHashMap<NMSBlockData>,
        onComplete: ((World) -> Unit)? = null
    ) {
        val worldQueue = Long2ObjectOpenHashMap<Array<Array<Array<IBlockData>>>>()
        raw.addToWorldQueue(queue, worldQueue)
        raw.placeWorldQueue(world, worldQueue, onComplete, true)
    }
}
