package net.horizonsend.ion.server.features.transport.node.manager

import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.horizonsend.ion.server.features.transport.NewTransport
import net.horizonsend.ion.server.features.transport.cache.FluidTransportCache
import net.horizonsend.ion.server.features.transport.cache.PowerTransportCache
import net.horizonsend.ion.server.features.transport.node.manager.extractors.ShipExtractorManager
import net.horizonsend.ion.server.features.transport.node.manager.holders.ShipNetworkHolder

class ShipTransportManager(val starship: Starship) : TransportManager() {
	override val extractorManager: ShipExtractorManager = ShipExtractorManager(starship)

	override val powerNodeManager = ShipNetworkHolder(this) { PowerTransportCache(it) }
	override val fluidNodeManager = ShipNetworkHolder(this) { FluidTransportCache(it) }

	init {
		load()
	}

	fun load() {
		NewTransport.registerTransportManager(this)
		extractorManager.loadExtractors()
	}

	fun release() {
		NewTransport.removeTransportManager(this)
		extractorManager.releaseExtractors()
	}

	fun displace(movement: StarshipMovement) {

	}
}
