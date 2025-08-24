package net.horizonsend.ion.server.features.transport.manager.graph

import net.horizonsend.ion.server.features.transport.nodes.graph.TransportNode
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer

abstract class TransportNodeType<T: TransportNode> {
	abstract fun serialize(raw: T, adapterContext: PersistentDataAdapterContext): PersistentDataContainer
	abstract fun deserialize(data: PersistentDataContainer, adapterContext: PersistentDataAdapterContext): T


}
