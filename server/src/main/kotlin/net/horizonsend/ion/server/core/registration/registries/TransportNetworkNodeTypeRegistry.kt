package net.horizonsend.ion.server.core.registration.registries

import net.horizonsend.ion.server.core.registration.keys.KeyRegistry
import net.horizonsend.ion.server.core.registration.keys.RegistryKeys
import net.horizonsend.ion.server.core.registration.keys.TransportNetworkNodeTypeKeys
import net.horizonsend.ion.server.features.transport.manager.graph.TransportNodeType

class TransportNetworkNodeTypeRegistry : Registry<TransportNodeType<*>>(RegistryKeys.TRANSPORT_NETWORK_NODE_TYPE) {
	override fun getKeySet(): KeyRegistry<TransportNodeType<*>> = TransportNetworkNodeTypeKeys

	override fun boostrap() {

	}
}
