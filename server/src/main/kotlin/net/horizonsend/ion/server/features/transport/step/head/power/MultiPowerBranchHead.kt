package net.horizonsend.ion.server.features.transport.step.head.power

import net.horizonsend.ion.server.features.transport.network.ChunkPowerNetwork
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.features.transport.step.head.HeadHolder
import net.horizonsend.ion.server.features.transport.step.head.MultiBranchHead

/**
 *
 *
 **/
class MultiPowerBranchHead(
	override val holder: HeadHolder<ChunkPowerNetwork>,
	override val share: Double,
	override val previousNodes: MutableSet<TransportNode> = mutableSetOf(),
	override val heads: MutableSet<MultiBranchHead.MultiHeadHolder<ChunkPowerNetwork>> = mutableSetOf(),
) : MultiBranchHead<ChunkPowerNetwork>, PowerBranchHead

