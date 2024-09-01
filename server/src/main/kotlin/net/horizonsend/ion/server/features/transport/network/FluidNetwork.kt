package net.horizonsend.ion.server.features.transport.network

import net.horizonsend.ion.server.features.transport.network.holders.NetworkHolder
import net.horizonsend.ion.server.features.transport.node.NetworkType
import net.horizonsend.ion.server.features.transport.node.gas.FluidExtractorNode
import net.horizonsend.ion.server.features.transport.node.gas.FluidNodeFactory
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.GAS_TRANSPORT
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import org.bukkit.NamespacedKey
import java.util.concurrent.ConcurrentHashMap

class FluidNetwork(holder: NetworkHolder<FluidNetwork>) : TransportNetwork(holder) {
	override val namespacedKey: NamespacedKey = GAS_TRANSPORT
	override val type: NetworkType = NetworkType.FLUID
	override val nodeFactory = FluidNodeFactory(this)

	val extractors: ConcurrentHashMap<BlockKey, FluidExtractorNode> = ConcurrentHashMap()

	override val dataVersion: Int = 0

	override suspend fun tick() {

	}

	override fun clearData() {
		nodes.clear()
		extractors.clear()
	}
}
