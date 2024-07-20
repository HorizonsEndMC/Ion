package net.horizonsend.ion.server.features.transport.node.gas

import net.horizonsend.ion.server.features.multiblock.util.BlockSnapshot
import net.horizonsend.ion.server.features.transport.network.GasNetwork
import net.horizonsend.ion.server.features.transport.node.NodeFactory
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey

class GasNodeFactory(network: GasNetwork) : NodeFactory<GasNetwork>(network) {
	override suspend fun create(key: BlockKey, snapshot: BlockSnapshot) {

	}
}
