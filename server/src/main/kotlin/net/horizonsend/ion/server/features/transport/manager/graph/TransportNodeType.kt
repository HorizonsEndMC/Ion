package net.horizonsend.ion.server.features.transport.manager.graph

import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.core.registration.keys.TransportNetworkNodeTypeKeys
import net.horizonsend.ion.server.features.transport.nodes.graph.TransportNode
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer

abstract class TransportNodeType<T: TransportNode>(val key: IonRegistryKey<TransportNodeType<*>, out TransportNodeType<T>>) {
	protected abstract fun serializeData(node: T, adapterContext: PersistentDataAdapterContext): PersistentDataContainer

	@Suppress("UNCHECKED_CAST")
	fun serializeUnsafe(node: TransportNode, context: PersistentDataAdapterContext): PersistentDataContainer {
		val data = serializeData(node as T, context)
		data.set(NamespacedKeys.NODE_TYPE, TransportNetworkNodeTypeKeys.serializer, key)
		return data
	}

	abstract fun deserialize(data: PersistentDataContainer, adapterContext: PersistentDataAdapterContext): T
}
