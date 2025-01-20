package net.horizonsend.ion.server.features.transport.manager

import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.horizonsend.ion.server.features.transport.NewTransport
import net.horizonsend.ion.server.features.transport.manager.extractors.ShipExtractorManager
import net.horizonsend.ion.server.features.transport.manager.holders.ShipCacheHolder
import net.horizonsend.ion.server.features.transport.nodes.cache.ItemTransportCache
import net.horizonsend.ion.server.features.transport.nodes.cache.PowerTransportCache
import net.horizonsend.ion.server.features.transport.nodes.cache.SolarPanelCache
import net.horizonsend.ion.server.features.transport.nodes.inputs.InputManager
import net.horizonsend.ion.server.features.transport.nodes.inputs.ShipInputManager
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i

class ShipTransportManager(val starship: Starship) : TransportManager<ShipCacheHolder<*>>() {
	override val extractorManager: ShipExtractorManager = ShipExtractorManager(this)
	val inputManager = ShipInputManager(this)

	override val powerNodeManager = ShipCacheHolder(this) { PowerTransportCache(it) }
	override val solarPanelManager = ShipCacheHolder(this) { SolarPanelCache(it) }
	override val itemPipeManager = ShipCacheHolder(this) { ItemTransportCache(it) }
//	override val fluidNodeManager = ShipCacheHolder(this) { FluidTransportCache(it) }

	override val networks: Array<ShipCacheHolder<*>> = arrayOf(
		powerNodeManager,
		solarPanelManager,
		itemPipeManager,
//		fluidNodeManager
	)

	init {
		load()
	}

	fun load() {
		networks.forEach { it.handleLoad() }
		extractorManager.loadExtractors()
		NewTransport.registerTransportManager(this)
	}

	fun release() {
		networks.forEach { it.release() }
		NewTransport.removeTransportManager(this)
		extractorManager.releaseExtractors()
	}

	fun displace(movement: StarshipMovement) {
		networks.forEach { it.displace(movement) }
	}

	override fun getInputProvider(): InputManager {
		return inputManager
	}

	override fun getGlobalCoordinate(localVec3i: Vec3i): Vec3i {
		return starship.getGlobalCoordinate(localVec3i)
	}

	override fun getLocalCoordinate(globalVec3i: Vec3i): Vec3i {
		return starship.getLocalCoordinate(globalVec3i)
	}
}
