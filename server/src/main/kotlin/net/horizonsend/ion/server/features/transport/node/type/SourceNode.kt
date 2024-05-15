package net.horizonsend.ion.server.features.transport.node.type

import net.horizonsend.ion.server.features.transport.step.PowerOriginStep

/**
 * Representing the start of a power system
 *
 * Nodes may not transfer into a source node
 **/
interface SourceNode {
	suspend fun startStep(): PowerOriginStep?
}
