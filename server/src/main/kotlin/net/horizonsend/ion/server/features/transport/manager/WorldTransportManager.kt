package net.horizonsend.ion.server.features.transport.manager

import net.horizonsend.ion.server.features.transport.NewTransport
import net.horizonsend.ion.server.features.transport.NewTransport.registerTransportManager
import net.horizonsend.ion.server.features.transport.inputs.InputManager
import net.horizonsend.ion.server.features.transport.manager.graph.FluidGraphManager
import net.horizonsend.ion.server.features.world.IonWorld
import org.bukkit.World

class WorldTransportManager(val world: IonWorld) : TransportHolder {
	val fluidGraphManager = FluidGraphManager(this)

	override fun getInputProvider(): InputManager {
		return world.inputManager
	}

	override fun getWorld(): World {
		return world.world
	}

	fun load() {
		registerTransportManager(this)
	}

	fun unload() {
		NewTransport.removeTransportManager(this)
	}

	override fun tick() {
		tryTickGraphs()
	}

	fun tryTickGraphs() {
		try {
			fluidGraphManager.tick()
		} catch (e: Throwable) {
			e.printStackTrace()
		}
	}
}
