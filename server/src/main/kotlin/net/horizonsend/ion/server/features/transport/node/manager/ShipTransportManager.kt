package net.horizonsend.ion.server.features.transport.node.manager

import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.horizonsend.ion.server.features.transport.node.manager.holders.ShipNetworkHolder
import net.horizonsend.ion.server.features.transport.node.manager.node.FluidNodeManager
import net.horizonsend.ion.server.features.transport.node.manager.node.PowerNodeManager

class ShipTransportManager(val starship: Starship) : TransportManager() {
	override val powerNodeManager = ShipNetworkHolder(this) { PowerNodeManager(it) }
	override val fluidNodeManager = ShipNetworkHolder(this) { FluidNodeManager(it) }

	init {
		load()
	}

	fun load() {

	}

	fun displace(movement: StarshipMovement) {

	}
}
