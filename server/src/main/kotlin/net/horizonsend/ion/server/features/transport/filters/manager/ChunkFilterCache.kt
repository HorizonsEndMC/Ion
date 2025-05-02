package net.horizonsend.ion.server.features.transport.filters.manager

import net.horizonsend.ion.server.features.transport.manager.ChunkTransportManager

class ChunkFilterCache(override val manager: ChunkTransportManager) : FilterCache(manager) {
}
