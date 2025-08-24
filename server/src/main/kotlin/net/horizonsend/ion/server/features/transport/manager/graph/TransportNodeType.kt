package net.horizonsend.ion.server.features.transport.manager.graph

import net.horizonsend.ion.server.features.transport.nodes.graph.TransportNode
import org.bukkit.persistence.PersistentDataContainer

abstract class TransportNodeType<T: TransportNode> {
	abstract fun serialize(raw: T): PersistentDataContainer
	abstract fun deserialize(data: PersistentDataContainer): T


}
