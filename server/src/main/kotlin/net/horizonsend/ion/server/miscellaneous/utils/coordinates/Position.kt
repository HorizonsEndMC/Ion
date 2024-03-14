package net.horizonsend.ion.server.miscellaneous.utils.coordinates

@Deprecated("")
data class Position<T : Number>(
	val x: T,
	val y: T,
	val z: T
)
