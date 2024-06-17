package net.horizonsend.ion.server.features.multiblock.util

import kotlinx.coroutines.future.asDeferred
import net.horizonsend.ion.server.features.multiblock.util.BlockSnapshot.Companion.getBlockSnapshot
import net.horizonsend.ion.server.miscellaneous.utils.getChunkAtIfLoaded
import org.bukkit.ChunkSnapshot
import org.bukkit.World

object AsyncTick {
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
		return getChunkSnapshotAsync(world, x, y, z)?.getBlockSnapshot(x, y, z)
	}

}
