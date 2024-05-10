package net.horizonsend.ion.server.features.world.data

import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntityFixer
import net.horizonsend.ion.server.features.world.chunk.IonChunk

object ChunkDataFixer {
	const val DATA_VERSION = 1

	fun upgrade(chunk: IonChunk) {
		if (chunk.dataVersion == DATA_VERSION) return
		if (chunk.dataVersion > DATA_VERSION) throw IllegalStateException("Invalid chunk data version!")

		MultiblockEntityFixer.upgrade(chunk)
	}
}
