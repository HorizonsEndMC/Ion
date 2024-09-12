package net.horizonsend.ion.server.features.transport.grid.sink

interface PowerSource : Source {
	fun getTransferablePower(): Int
}
