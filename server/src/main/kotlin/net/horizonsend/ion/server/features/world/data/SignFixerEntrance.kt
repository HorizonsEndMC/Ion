package net.horizonsend.ion.server.features.world.data

import net.horizonsend.ion.server.features.world.chunk.IonChunk
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.isWallSign
import org.bukkit.block.Sign

object SignFixerEntrance {
	fun iterateChunk(chunk: IonChunk) = Tasks.async {
		val chunkSnapshot = chunk.inner.getChunkSnapshot(true, false, false)
		val signBlocks = mutableSetOf<Vec3i>()

		for (x in 0..15) for (z in 0..15) {
			val minBlockY = chunk.world.minHeight
			val maxBlockY = chunkSnapshot.getHighestBlockYAt(x, z)

			for (y in minBlockY..maxBlockY) {
				val blockType = chunkSnapshot.getBlockType(x, y, z)

				if (!blockType.isWallSign) continue
				signBlocks.add(Vec3i(x, y, z))
			}
		}

		Tasks.sync {
			for ((x, y, z) in signBlocks) {
				val block = chunk.inner.getBlock(x, y, z)
				val state = block.state as Sign

				DataFixers.handleMultiblockSignLoad(state)
			}
		}
	}
}
