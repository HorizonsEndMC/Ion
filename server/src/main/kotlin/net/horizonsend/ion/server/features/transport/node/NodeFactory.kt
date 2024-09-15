package net.horizonsend.ion.server.features.transport.node

import net.horizonsend.ion.server.features.transport.node.manager.node.NodeManager
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import org.bukkit.block.data.BlockData

abstract class NodeFactory<T: NodeManager>(val network: T) {
	/**
	 * Create and handle placement of a node at the position, if one should be created
	 **/
	abstract fun create(key: BlockKey, data: BlockData): Boolean
}
