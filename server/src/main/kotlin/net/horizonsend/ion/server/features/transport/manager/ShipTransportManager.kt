package net.horizonsend.ion.server.features.transport.manager

import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.horizonsend.ion.server.features.transport.NewTransport
import net.horizonsend.ion.server.features.transport.manager.extractors.ShipExtractorManager
import net.horizonsend.ion.server.features.transport.manager.holders.ShipCacheHolder
import net.horizonsend.ion.server.features.transport.nodes.cache.PowerTransportCache
import net.horizonsend.ion.server.features.transport.nodes.cache.solarpanel.SolarPanelCache
import net.horizonsend.ion.server.features.transport.nodes.inputs.InputManager
import net.horizonsend.ion.server.features.transport.nodes.inputs.ShipInputManager

class ShipTransportManager(val starship: Starship) : TransportManager() {
	override val extractorManager: ShipExtractorManager = ShipExtractorManager(starship)
	val inputManager = ShipInputManager(this)

	override val powerNodeManager = ShipCacheHolder(this) { PowerTransportCache(it) }
	override val solarPanelManager: ShipCacheHolder<SolarPanelCache> = ShipCacheHolder(this) { SolarPanelCache(it) }
//	override val fluidNodeManager = ShipCacheHolder(this) { FluidTransportCache(it) }

	init {
		load()
	}

	fun load() {
		powerNodeManager.capture()
		solarPanelManager.capture()
//		fluidNodeManager.capture()
		extractorManager.loadExtractors()
		NewTransport.registerTransportManager(this)
	}

	fun release() {
		powerNodeManager.release()
		solarPanelManager.release()
//		fluidNodeManager.release()
		extractorManager.releaseExtractors()
		NewTransport.removeTransportManager(this)
	}

	fun displace(movement: StarshipMovement) {
		powerNodeManager.displace(movement)
		solarPanelManager.displace(movement)
//		fluidNodeManager.displace(movement)
		extractorManager.displace(movement)
	}

	override fun getInputProvider(): InputManager {
		return inputManager
	}
}
