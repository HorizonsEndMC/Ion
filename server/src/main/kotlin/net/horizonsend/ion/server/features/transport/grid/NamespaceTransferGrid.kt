package net.horizonsend.ion.server.features.transport.grid

import net.horizonsend.ion.server.features.transport.ChunkTransportNetwork
import org.bukkit.NamespacedKey

abstract class NamespaceTransferGrid(
	network: ChunkTransportNetwork,
	vararg namespaces: NamespacedKey
) : Grid(network) {
	val valueKeys = setOf(*namespaces)

	fun isTransferable(resourceKey: NamespacedKey): Boolean = valueKeys.contains(resourceKey)

	fun getTransferableResources(): Set<NamespacedKey> = TODO()

	class ResourceContainer(

	)
}
