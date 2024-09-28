package net.horizonsend.ion.server.features.transport.node.type.fluid

import net.horizonsend.ion.server.features.transport.fluids.PipedFluid
import net.horizonsend.ion.server.features.transport.node.NodeType
import net.horizonsend.ion.server.features.transport.node.NodeType.FLUID_EXTRACTOR_NODE
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.features.transport.node.manager.FluidNodeManager
import net.horizonsend.ion.server.features.transport.node.type.general.ExtractorNode
import net.horizonsend.ion.server.features.transport.node.type.general.UnTransferableNode
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.toMap

class FluidExtractorNode(override val manager: FluidNodeManager) : ExtractorNode(), FluidPathfindingNode, UnTransferableNode {
	override val type: NodeType = FLUID_EXTRACTOR_NODE

	override fun getPathfindingResistance(previousNode: TransportNode?, nextNode: TransportNode?): Int = 0

	override fun getNextNodes(previous: TransportNode, destination: TransportNode?, resource: PipedFluid): ArrayDeque<TransportNode> = cachedTransferable

	override fun isTransferableTo(node: TransportNode): Boolean {
		return node !is UnTransferableNode
	}

	override fun loadIntoNetwork() {
		super.loadIntoNetwork()
		manager.extractors[position] = this
	}

	override fun onPlace(position: BlockKey) {
		super.onPlace(position)
		manager.extractors[position] = this
	}

	override fun canTransfer(resource: PipedFluid): Boolean {
		return false
	}

	fun getSourcePool() = relationHolder.getAllOthers().mapNotNull { it.other as? FluidInputNode }.flatMap { it.getEntities() }

	fun getAvailableFluidTypes(): List<PipedFluid> {
		val sources = getSourcePool()

		val new = mutableListOf<PipedFluid>()
		for (source in sources) {
			for ((resource, _) in source.getStoredResources()) {
				new.add(resource ?: continue)
			}
		}

		return new
	}

	fun getAvailableFluids() = getSourcePool().flatMap { it.getStoredResources().entries }.toMap()
}
