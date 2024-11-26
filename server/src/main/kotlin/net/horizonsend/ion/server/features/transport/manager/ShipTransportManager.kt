package net.horizonsend.ion.server.features.transport.manager

import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.horizonsend.ion.server.features.transport.NewTransport
import net.horizonsend.ion.server.features.transport.manager.extractors.ShipExtractorManager
import net.horizonsend.ion.server.features.transport.manager.holders.CacheHolder
import net.horizonsend.ion.server.features.transport.manager.holders.ShipCacheHolder
import net.horizonsend.ion.server.features.transport.nodes.cache.FluidTransportCache
import net.horizonsend.ion.server.features.transport.nodes.cache.PowerTransportCache
import net.horizonsend.ion.server.features.transport.nodes.cache.solarpanel.SolarPanelCache
import net.horizonsend.ion.server.features.transport.nodes.inputs.InputManager
import net.horizonsend.ion.server.features.transport.nodes.inputs.ShipInputManager

class ShipTransportManager(val starship: Starship) : TransportManager() {
	override val extractorManager: ShipExtractorManager = ShipExtractorManager(starship)
	private val inputManager = ShipInputManager(this)

	override val powerNodeManager = ShipCacheHolder(this) { PowerTransportCache(it) }
	override val solarPanelManager: CacheHolder<SolarPanelCache> = ShipCacheHolder(this) { SolarPanelCache(it) }
	override val fluidNodeManager = ShipCacheHolder(this) { FluidTransportCache(it) }

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

	override fun getInputProvider(): InputManager {
		return inputManager
	}
}
