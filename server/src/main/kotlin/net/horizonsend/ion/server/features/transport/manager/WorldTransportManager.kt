package net.horizonsend.ion.server.features.transport.manager

import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.transport.NewTransport
import net.horizonsend.ion.server.features.transport.NewTransport.registerTransportManager
import net.horizonsend.ion.server.features.transport.inputs.IOManager
import net.horizonsend.ion.server.features.transport.manager.graph.FluidNetworkManager
import net.horizonsend.ion.server.features.transport.manager.graph.GridEnergyGraphManager
import net.horizonsend.ion.server.features.world.IonWorld
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import org.bukkit.World
import org.bukkit.persistence.PersistentDataContainer
import java.util.function.Consumer

class WorldTransportManager(val world: IonWorld) : TransportHolder {
	val fluidGraphManager = FluidNetworkManager(this)
	val gridEnergyGraphManager = GridEnergyGraphManager(this)

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
			gridEnergyGraphManager.tick()
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

	override fun getMultiblockmanager(globalVec3i: Vec3i): MultiblockManager? {
		return world.getChunkFromWorldcoordinates(globalVec3i.x, globalVec3i.z)?.multiblockManager
	}
}
