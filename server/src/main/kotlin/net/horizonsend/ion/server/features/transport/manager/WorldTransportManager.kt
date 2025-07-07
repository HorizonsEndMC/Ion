package net.horizonsend.ion.server.features.transport.manager

import net.horizonsend.ion.server.features.transport.NewTransport
import net.horizonsend.ion.server.features.transport.NewTransport.registerTransportManager
import net.horizonsend.ion.server.features.transport.inputs.IOManager
import net.horizonsend.ion.server.features.transport.manager.graph.FluidNetworkManager
import net.horizonsend.ion.server.features.world.IonWorld
import org.bukkit.World
import org.bukkit.persistence.PersistentDataContainer
import java.util.function.Consumer

class WorldTransportManager(val world: IonWorld) : TransportHolder {
	val fluidGraphManager = FluidNetworkManager(this)

	override fun getInputProvider(): IOManager {
		return world.inputManager
	}

	override fun getWorld(): World {
		return world.world
	}

	fun load() {
		registerTransportManager(this)
	}

	fun unload() {
		// Stop ticking then save
		NewTransport.removeTransportManager(this)
		save()
	}

	override fun tickExtractors() {}

	override fun tickGraphs() {
		try {
			fluidGraphManager.tick()
		} catch (e: Throwable) {
			e.printStackTrace()
		}
	}

	override fun storePersistentData(storeConsumer: Consumer<PersistentDataContainer>) {
		storeConsumer.accept(world.world.persistentDataContainer)
	}

	fun save() {
		fluidGraphManager.save(world.world.persistentDataContainer.adapterContext)
	}
}
