package net.horizonsend.ion.server.features.world.generation

import net.horizonsend.ion.server.features.space.data.StoredChunkBlocks
import net.horizonsend.ion.server.features.space.data.StoredChunkBlocks.Companion.place
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import org.bukkit.Chunk

fun regenerateChunk(chunk: Chunk) = chunk.persistentDataContainer
	.get(NamespacedKeys.STORED_CHUNK_BLOCKS, StoredChunkBlocks)
	?.place(chunk)
