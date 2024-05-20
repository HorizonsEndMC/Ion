package net.horizonsend.ion.server.features.transport.node.type

import net.horizonsend.ion.server.features.transport.step.OriginStep

/**
 * Representing the start of a power system
 *
 * Nodes may not transfer into a source node
 **/
interface SourceNode {
	suspend fun startStep(): OriginStep?
}
