package net.horizonsend.ion.server.features.world.data

import net.horizonsend.ion.server.features.world.chunk.IonChunk

interface ChunkDataFixer : DataFixer {
	fun fix(chunk: IonChunk)
}
