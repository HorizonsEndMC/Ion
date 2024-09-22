package net.horizonsend.ion.server.features.transport.node.manager

import net.horizonsend.ion.server.features.transport.node.manager.holders.NetworkHolder
import net.horizonsend.ion.server.features.transport.node.type.fluid.FluidNodeFactory
import net.horizonsend.ion.server.features.transport.node.util.NetworkType
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.FLUID_TRANSPORT
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import org.bukkit.NamespacedKey
import java.util.concurrent.ConcurrentHashMap

class FluidNodeManager(holder: NetworkHolder<FluidNodeManager>) : NodeManager(holder) {
	override val namespacedKey: NamespacedKey = FLUID_TRANSPORT
	override val type: NetworkType = NetworkType.FLUID
	override val nodeFactory = FluidNodeFactory(this)

	val extractors: ConcurrentHashMap<BlockKey, net.horizonsend.ion.server.features.transport.node.type.fluid.FluidExtractorNode> = ConcurrentHashMap()

	override val dataVersion: Int = 0

	override fun clearData() {
		nodes.clear()
		extractors.clear()
	}
}
