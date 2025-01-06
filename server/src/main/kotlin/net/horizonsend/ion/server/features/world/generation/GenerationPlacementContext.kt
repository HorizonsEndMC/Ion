package net.horizonsend.ion.server.features.world.generation

/**
 * Returns a Y value to place the feature at
 * @param worldMinHeight inclusive
 * @param worldMaxHeight exclusive
 **/
data class GenerationPlacementContext(
	val worldMinHeight : Int,
	val worldMaxHeight : Int,
	val x: Int,
	val z: Int,
	val surfaceY: Int
)
