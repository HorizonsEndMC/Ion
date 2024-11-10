package net.horizonsend.ion.server.features.transport.node.manager

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.features.transport.NewTransport
import net.horizonsend.ion.server.features.transport.cache.FluidTransportCache
import net.horizonsend.ion.server.features.transport.cache.TransportCache
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.features.transport.node.manager.holders.NetworkHolder
import net.horizonsend.ion.server.features.transport.node.type.fluid.FluidExtractorNode
import net.horizonsend.ion.server.features.transport.node.type.fluid.FluidInputNode
import net.horizonsend.ion.server.features.transport.node.type.fluid.FluidNodeFactory
import net.horizonsend.ion.server.features.transport.node.util.NetworkType
import net.horizonsend.ion.server.features.transport.node.util.getNetworkDestinations
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.FLUID_TRANSPORT
import org.bukkit.NamespacedKey
import java.util.concurrent.Future

class FluidNodeManager(holder: NetworkHolder<FluidTransportCache>) : NodeManager<FluidExtractorNode>(holder) {
	override val namespacedKey: NamespacedKey = FLUID_TRANSPORT
	override val type: NetworkType = NetworkType.FLUID
	override val nodeFactory = FluidNodeFactory(this)

	override val dataVersion: Int = 0

	override fun clearData() {
		nodes.clear()
		extractors.clear()
	}

	fun tickTransport() {
		extractors.values.forEach(::tickExtractor)
	}

	private fun tickExtractor(extractorNode: FluidExtractorNode): Future<*> = NewTransport.executor.submit {
		val transferCheck = extractorNode.getTransferAmount()
		if (transferCheck == 0) return@submit

		extractorNode.markTicked()

		val source = extractorNode.getSourcePool().filterNot { it.isFull() }.randomOrNull() ?: return@submit

		val destinations: ObjectOpenHashSet<FluidInputNode> = getFluidInputs(extractorNode)
		destinations.removeAll(extractorNode.getTransferableNodes().filterIsInstanceTo(ObjectOpenHashSet()))

		if (destinations.isEmpty()) return@submit

//		val transferred = minOf(source.getPower(), check)
//		val notRemoved = source.storage.removePower(transferred)
//		val remainder = runPowerTransfer(extractorNode, destinations.toMutableList(), (transferred - notRemoved))
//
//		if (transferred == remainder) {
//			//TODO skip growing number of ticks if nothing to do
//		}
//
//		if (remainder > 0) {
//			source.storage.addPower(remainder)
//		}
	}
}

fun getFluidInputs(origin: TransportNode) = getNetworkDestinations<FluidInputNode>(origin) { it.isCalling() }
