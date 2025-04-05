package net.horizonsend.ion.server.features.transport.nodes.types

import net.horizonsend.ion.server.features.starship.movement.StarshipMovement

interface ComplexNode {
	fun displace(movement: StarshipMovement)
}
