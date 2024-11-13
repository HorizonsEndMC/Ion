package net.horizonsend.ion.server.features.transport.nodes.cache

import net.horizonsend.ion.server.features.starship.movement.StarshipMovement

interface ComplexCachedNode {
	fun onTranslate(movement: StarshipMovement)
}
