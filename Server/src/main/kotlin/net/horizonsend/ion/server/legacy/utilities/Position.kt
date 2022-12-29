package net.horizonsend.ion.server.legacy.utilities

@Deprecated("")
data class Position<T : Number>(
	val x: T,
	val y: T,
	val z: T
)