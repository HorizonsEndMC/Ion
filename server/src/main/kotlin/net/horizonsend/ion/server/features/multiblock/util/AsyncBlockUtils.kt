package net.horizonsend.ion.server.features.multiblock.util

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.future.asDeferred
import net.horizonsend.ion.server.features.multiblock.util.BlockSnapshot.Companion.getBlockSnapshot
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.getChunkAtIfLoaded
import org.bukkit.ChunkSnapshot
import org.bukkit.World
import org.bukkit.block.Block

suspend fun getChunkSnapshotAsync(world: World, x: Int, y: Int, z: Int, loadChunks: Boolean = false): ChunkSnapshot? {
	val chunk = if (loadChunks) {
		world.getChunkAtAsync(x shr 4, z shr 4).asDeferred().await()
	} else {
		world.getChunkAtIfLoaded(x shr 4, z shr 4) ?: return null
	}

	return chunk.chunkSnapshot
}

/** Retrieves a snapshot of an async block */
suspend fun getBlockSnapshotAsync(world: World, x: Int, y: Int, z: Int, loadChunks: Boolean = false): BlockSnapshot? {
	return getChunkSnapshotAsync(world, x, y, z, loadChunks)?.getBlockSnapshot(x, y, z)
}

fun getBlockAsync(world: World, x: Int, y: Int, z: Int): Deferred<Block> {
	val deferred = CompletableDeferred<Block>()

	Tasks.async {
		deferred.complete(world.getBlockAt(x, y, z))
	}

	return deferred
}

suspend fun <K, V> Map<K, Deferred<V>>.awaitAllValues(): Map<K, V> = if (isEmpty()) mapOf() else mapValues { (_, v) -> v.await() }
