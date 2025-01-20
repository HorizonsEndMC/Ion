package net.horizonsend.ion.server.features.transport.nodes.types

interface FilterNode <T: Any> : Node {
	fun canTransfer(resource: T): Boolean
}
