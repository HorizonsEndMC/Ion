package net.horizonsend.ion.server.features.transport.manager.graph.gridenergy

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.gridenergy.GridEnergyMultiblock
import net.horizonsend.ion.server.features.multiblock.entity.type.gridenergy.GridEnergyPortMetaData
import net.horizonsend.ion.server.features.transport.inputs.IOPort
import net.horizonsend.ion.server.features.transport.inputs.IOType
import net.horizonsend.ion.server.features.transport.manager.graph.NetworkManager
import net.horizonsend.ion.server.features.transport.manager.graph.TransportNetwork
import net.horizonsend.ion.server.features.transport.manager.graph.fluid.FluidGraphEdge
import net.horizonsend.ion.server.features.transport.nodes.graph.GraphEdge
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import java.util.UUID

class GridEnergyNetwork(uuid: UUID, override val manager: NetworkManager<GridEnergyNode, TransportNetwork<GridEnergyNode>>) : TransportNetwork<GridEnergyNode>(uuid, manager) {
	override fun createEdge(
        nodeOne: GridEnergyNode,
        nodeTwo: GridEnergyNode
	): GraphEdge {
		return FluidGraphEdge(nodeOne, nodeTwo)
	}

	private var lastStructureTick: Long = System.currentTimeMillis()
	private var lastDisplayTick: Long = System.currentTimeMillis()

	override fun handleTick() {
		val now = System.currentTimeMillis()

		if (now - lastStructureTick > STRUCTURE_INTERVAL) {
			lastStructureTick = now

			// Discover any strucural changes and check integrity of the network
			discoverNetwork()
		}

		reCalculatePower()
	}

	var lastConsumption = 0.0; private set

	fun reCalculatePower(): Double {
		val (inputs, outputs) = trackIO()

		val availableInput = getNetworkInputPower(outputs)
		return checkPowerConsumption(inputs, availableInput)
	}

	fun hasAvailablePower(bonusConsumption: Double): Double {
		val (inputs, outputs) = trackIO()

		val availableInput = getNetworkInputPower(outputs)
		return checkPowerConsumption(inputs, availableInput, update = false, bonusConsumption = bonusConsumption)
	}

	private fun getNetworkInputPower(multiblockOutputs: ObjectOpenHashSet<IOPort.RegisteredMetaDataInput<GridEnergyPortMetaData>>): Double {
		val checkedEntities = ObjectOpenHashSet<MultiblockEntity>()

		var provided = 0.0

		for (output in multiblockOutputs) {
			val entity = output.holder
			if (entity !is GridEnergyMultiblock) continue
			if (checkedEntities.contains(entity)) continue
			checkedEntities.add(entity)

			provided += entity.getGridEnergyOutput()
		}

		return provided
	}

	private fun checkPowerConsumption(multiblockInputs: ObjectOpenHashSet<IOPort.RegisteredMetaDataInput<GridEnergyPortMetaData>>, availablePower: Double, update: Boolean = true, bonusConsumption: Double = 0.0): Double {
		val checkedEntities = ObjectOpenHashSet<GridEnergyMultiblock>()

		var consumed = 0.0

		for (input in multiblockInputs) {
			val entity = input.holder
			if (entity !is GridEnergyMultiblock) continue
			if (checkedEntities.contains(entity)) continue
			checkedEntities.add(entity)

			consumed += entity.getTotalGridEnergyConsumption()
		}

		val availablePercentage = availablePower / (consumed + bonusConsumption)

		lastConsumption = consumed

		if (!update) return availablePercentage

		for (entity in checkedEntities) {
			entity.markPowerShortage(availablePercentage)
			entity.gridEnergyManager.runUpdates()
		}

		return availablePercentage
	}

	override fun save(adapterContext: PersistentDataAdapterContext): PersistentDataContainer {
		return adapterContext.newPersistentDataContainer()
	}

	/**
	 * Returns a pair of a location map of inputs, and a location map of outputs
	 **/
	private fun trackIO(): Pair<ObjectOpenHashSet<IOPort.RegisteredMetaDataInput<GridEnergyPortMetaData>>, ObjectOpenHashSet<IOPort.RegisteredMetaDataInput<GridEnergyPortMetaData>>> {
		val inputs = ObjectOpenHashSet<IOPort.RegisteredMetaDataInput<GridEnergyPortMetaData>>()
		val outputs = ObjectOpenHashSet<IOPort.RegisteredMetaDataInput<GridEnergyPortMetaData>>()

		for (node in getGraphNodes()) {
			val ports: ObjectOpenHashSet<IOPort.RegisteredMetaDataInput<GridEnergyPortMetaData>> = manager.transportManager.getInputProvider().getPorts(IOType.GRID_ENERGY, node.location)

			for (port in ports) {
				val metaData = port.metaData
				if (metaData.inputAllowed) inputs.add(port)
				if (metaData.outputAllowed) outputs.add(port)
			}
		}

		return inputs to outputs
	}

	companion object {
		private const val STRUCTURE_INTERVAL = 1000L
		private const val DISPLAY_INTERVAL = 250L
	}
}
