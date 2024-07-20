package net.horizonsend.ion.server.features.transport.network

import net.horizonsend.ion.server.features.transport.ChunkTransportManager
import net.horizonsend.ion.server.features.transport.node.NetworkType
import net.horizonsend.ion.server.features.transport.node.gas.GasExtractorNode
import net.horizonsend.ion.server.features.transport.node.gas.GasNodeFactory
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.GAS_TRANSPORT
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import org.bukkit.NamespacedKey
import java.util.concurrent.ConcurrentHashMap

class ChunkGasNetwork(manager: ChunkTransportManager) : ChunkTransportNetwork(manager) {
	override val namespacedKey: NamespacedKey = GAS_TRANSPORT
	override val type: NetworkType = NetworkType.GAS
	override val nodeFactory = GasNodeFactory(this)

	val extractors: ConcurrentHashMap<BlockKey, GasExtractorNode> = ConcurrentHashMap()

	override val dataVersion: Int = 0

	override suspend fun tick() {

	}

	override fun clearData() {
		nodes.clear()
		extractors.clear()
	}
}
