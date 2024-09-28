package net.horizonsend.ion.server.features.transport.node.manager

import net.horizonsend.ion.server.features.transport.NewTransport
import net.horizonsend.ion.server.features.transport.node.manager.holders.NetworkHolder
import net.horizonsend.ion.server.features.transport.node.type.fluid.FluidExtractorNode
import net.horizonsend.ion.server.features.transport.node.type.fluid.FluidNodeFactory
import net.horizonsend.ion.server.features.transport.node.util.NetworkType
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.FLUID_TRANSPORT
import org.bukkit.NamespacedKey
import java.util.concurrent.Future

class FluidNodeManager(holder: NetworkHolder<FluidNodeManager>) : NodeManager<FluidExtractorNode>(holder) {
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
		val check = extractorNode.getTransferAmount()
		if (check == 0) return@submit

		extractorNode.markTicked()

//		val source = extractorNode.getSourcePool().filterNot { it.storage.isEmpty() }.randomOrNull() ?: return@submit
//
//		val destinations: ObjectOpenHashSet<PowerInputNode> = getPowerInputs(extractorNode)
//		destinations.removeAll(extractorNode.getTransferableNodes().filterIsInstanceTo(ObjectOpenHashSet()))
//
//		if (destinations.isEmpty()) return@submit
//
//		val transferred = minOf(source.storage.getPower(), check)
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
