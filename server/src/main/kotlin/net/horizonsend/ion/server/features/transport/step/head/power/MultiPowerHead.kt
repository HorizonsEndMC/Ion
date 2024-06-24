package net.horizonsend.ion.server.features.transport.step.head.power

import net.horizonsend.ion.server.features.transport.network.ChunkPowerNetwork
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.features.transport.step.head.MultiHead
import net.horizonsend.ion.server.features.transport.step.new.NewStep

/**
 *
 *
 **/
class MultiPowerHead(
	override val share: Double,
	override val parent: NewStep<ChunkPowerNetwork>,
	override val coveredNodes: MutableSet<TransportNode> = mutableSetOf(),
	override val heads: MutableSet<MultiHead.MultiHeadHolder<ChunkPowerNetwork>> = mutableSetOf()
) : MultiHead<ChunkPowerNetwork>, PowerStepHead

