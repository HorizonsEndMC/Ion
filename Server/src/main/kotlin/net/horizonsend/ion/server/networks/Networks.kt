package net.horizonsend.ion.server.networks

import net.horizonsend.ion.server.networks.connections.DirectConnection
import net.horizonsend.ion.server.networks.connections.WiredConnection
import net.horizonsend.ion.server.networks.nodes.ComputerNode
import net.horizonsend.ion.server.networks.nodes.ExtractorNode
import net.horizonsend.ion.server.networks.nodes.IronFocusedNode
import net.horizonsend.ion.server.networks.nodes.Node
import net.horizonsend.ion.server.networks.nodes.RedstoneFocusedNode
import java.util.LinkedList
import java.util.Queue

var tickId = Int.MIN_VALUE

val removalQueue = LinkedList<Validatable>() as Queue<Validatable>

val nodeTypes = arrayOf(
	ComputerNode,
	ExtractorNode,
	IronFocusedNode,
	RedstoneFocusedNode,
	Node
)

val connectionTypes = arrayOf(
	DirectConnection,
	WiredConnection
)