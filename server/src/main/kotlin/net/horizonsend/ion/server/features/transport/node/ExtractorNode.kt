package net.horizonsend.ion.server.features.transport.node

import net.horizonsend.ion.server.features.transport.container.ResourceContainer
import net.horizonsend.ion.server.features.transport.grid.ChunkTransportNetwork
import org.bukkit.block.BlockFace
import java.util.concurrent.ConcurrentHashMap

abstract class ExtractorNode<T>(
	override val parentTransportNetwork: ChunkTransportNetwork,
	override val x: Int,
	override val y: Int,
	override val z: Int,
) : GridNode {
	override val transferableNeighbors: ConcurrentHashMap<BlockFace, GridNode> = ConcurrentHashMap()

	abstract fun getExtractableInventories(): Collection<ResourceContainer<T>>
}
