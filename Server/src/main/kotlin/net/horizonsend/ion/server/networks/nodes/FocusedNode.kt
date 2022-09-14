package net.horizonsend.ion.server.networks.nodes

import net.horizonsend.ion.server.networks.connections.AbstractConnection
import net.horizonsend.ion.server.networks.connections.DirectConnection

abstract class FocusedNode : AbstractNode() {
	override fun canStepTo(nextNode: AbstractNode, nextConnection: AbstractConnection): Boolean {
		return !(nextConnection is DirectConnection && nextNode is Node)
	}
}