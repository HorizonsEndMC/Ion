package net.horizonsend.ion.server.features.transport.manager.graph.gridenergy

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.gridenergy.GridEnergyMultiblock
import net.horizonsend.ion.server.features.multiblock.entity.type.gridenergy.GridEnergyPortMetaData
import net.horizonsend.ion.server.features.transport.inputs.IOPort
import net.horizonsend.ion.server.features.transport.inputs.IOPort.RegisteredMetaDataInput
import net.horizonsend.ion.server.features.transport.inputs.IOType
import net.horizonsend.ion.server.features.transport.manager.graph.FlowNode
import net.horizonsend.ion.server.features.transport.manager.graph.FlowTrackingTransportGraph
import net.horizonsend.ion.server.features.transport.manager.graph.NetworkManager
import net.horizonsend.ion.server.features.transport.manager.graph.TransportNetwork
import net.horizonsend.ion.server.features.transport.manager.graph.fluid.FluidGraphEdge
import net.horizonsend.ion.server.features.transport.nodes.graph.GraphEdge
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import java.util.UUID

class GridEnergyNetwork(
	uuid: UUID,
	override val manager: NetworkManager<GridEnergyNode, TransportNetwork<GridEnergyNode>>
) : FlowTrackingTransportGraph<GridEnergyNode, RegisteredMetaDataInput<GridEnergyPortMetaData>>(uuid, manager, IOType.GRID_ENERGY) {
	override fun createEdge(
        nodeOne: GridEnergyNode,
        nodeTwo: GridEnergyNode
	): GraphEdge {
		return FluidGraphEdge(nodeOne, nodeTwo)
	}

	private var lastStructureTick: Long = System.currentTimeMillis()

	override fun handleTick() {
		val now = System.currentTimeMillis()

		if (now - lastStructureTick > STRUCTURE_INTERVAL) {
			lastStructureTick = now

			// Discover any structural changes and check integrity of the network
			discoverNetwork()

			// Determine the direction and capacity for flow through the network
			edmondsKarp()
		}

		reCalculatePower()
	}

	var lastConsumption = 0.0; private set
	var lastProduction = 0.0; private set

	fun reCalculatePower(): Double {
		val (inputs, outputs) = trackIO()

		val availableInput = getNetworkInputPower(outputs)
		lastProduction = availableInput
		return checkPowerConsumption(inputs, availableInput)
	}

	private fun getNetworkInputPower(multiblockOutputs: ObjectOpenHashSet<RegisteredMetaDataInput<GridEnergyPortMetaData>>): Double {
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

	private fun checkPowerConsumption(multiblockInputs: Long2ObjectOpenHashMap<RegisteredMetaDataInput<GridEnergyPortMetaData>>, availablePower: Double, update: Boolean = true, bonusConsumption: Double = 0.0): Double {
		val checkedEntities = ObjectOpenHashSet<GridEnergyMultiblock>()

		var consumed = 0.0

		for ((inputLoc, input) in multiblockInputs) {
			val entity = input.holder
			if (entity !is GridEnergyMultiblock) continue
			if (checkedEntities.contains(entity)) continue
			checkedEntities.add(entity)

			val flow = getFlow(inputLoc)
			consumed += minOf(entity.getTotalGridEnergyConsumption(), flow)
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

	fun getAvailablePowerPercentage(portLocation: BlockKey, bonusConsumption: Double): Double {
		val (inputs, outputs) = trackIO()

		val availableInput = getNetworkInputPower(outputs)
		val flow = getFlow(portLocation)
		return checkPowerConsumption(inputs, minOf(flow, availableInput), update = false, bonusConsumption = bonusConsumption)
	}

	override fun save(adapterContext: PersistentDataAdapterContext): PersistentDataContainer {
		return adapterContext.newPersistentDataContainer()
	}

	/**
	 * Returns a pair of a location map of inputs, and a location map of outputs
	 **/
	private fun trackIO(): Pair<Long2ObjectOpenHashMap<RegisteredMetaDataInput<GridEnergyPortMetaData>>, ObjectOpenHashSet<RegisteredMetaDataInput<GridEnergyPortMetaData>>> {
		val inputs = Long2ObjectOpenHashMap<RegisteredMetaDataInput<GridEnergyPortMetaData>>()
		val outputs = ObjectOpenHashSet<RegisteredMetaDataInput<GridEnergyPortMetaData>>()

		for (node in getGraphNodes()) {
			val ports: ObjectOpenHashSet<RegisteredMetaDataInput<GridEnergyPortMetaData>> = manager.transportManager.getInputProvider().getPorts(IOType.GRID_ENERGY, node.location)

			for (port in ports) {
				val metaData = port.metaData
				if (metaData.inputAllowed) inputs[node.location] = port
				if (metaData.outputAllowed) outputs.add(port)
			}
		}

		return inputs to outputs
	}

	companion object {
		private const val STRUCTURE_INTERVAL = 1000L
	}

	override fun isSink(node: FlowNode, ioData: RegisteredMetaDataInput<GridEnergyPortMetaData>): Boolean {
		return ioData.metaData.inputAllowed
	}

	override fun isSource(node: FlowNode, ioData: RegisteredMetaDataInput<GridEnergyPortMetaData>): Boolean {
		return ioData.metaData.outputAllowed
	}
}
