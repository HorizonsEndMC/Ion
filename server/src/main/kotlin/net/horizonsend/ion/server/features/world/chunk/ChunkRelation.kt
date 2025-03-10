package net.horizonsend.ion.server.features.world.chunk

/**
 * Represents a border between two chunks
 **/
data class ChunkRelation(
	val holder: IonChunk,
	val other: IonChunk
)
