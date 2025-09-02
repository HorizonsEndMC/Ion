package net.horizonsend.ion.server.features.transport.manager.graph.e2

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.e2.E2Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.type.e2.E2PortMetaData
import net.horizonsend.ion.server.features.transport.inputs.IOPort
import net.horizonsend.ion.server.features.transport.inputs.IOType
import net.horizonsend.ion.server.features.transport.manager.graph.NetworkManager
import net.horizonsend.ion.server.features.transport.manager.graph.TransportNetwork
import net.horizonsend.ion.server.features.transport.manager.graph.fluid.FluidGraphEdge
import net.horizonsend.ion.server.features.transport.nodes.graph.GraphEdge
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import java.util.UUID

class E2Network(uuid: UUID, override val manager: NetworkManager<E2Node, TransportNetwork<E2Node>>) : TransportNetwork<E2Node>(uuid, manager) {
	override fun createEdge(
		nodeOne: E2Node,
		nodeTwo: E2Node
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

	fun reCalculatePower(): Double {
		val (inputs, outputs) = trackIO()

		val availableInput = getNetworkInputPower(outputs)
		return checkPowerConsumption(inputs, availableInput)
	}

	private fun getNetworkInputPower(multiblockOutputs: ObjectOpenHashSet<IOPort.RegisteredMetaDataInput<E2PortMetaData>>): Double {
		val checkedEntities = ObjectOpenHashSet<MultiblockEntity>()

		var provided = 0.0

		for (output in multiblockOutputs) {
			val entity = output.holder
			if (entity !is E2Multiblock) continue
			if (checkedEntities.contains(entity)) continue
			checkedEntities.add(entity)

			provided += entity.getE2Output()
		}

		return provided
	}

	private fun checkPowerConsumption(multiblockInputs: ObjectOpenHashSet<IOPort.RegisteredMetaDataInput<E2PortMetaData>>, availablePower: Double): Double {
		val checkedEntities = ObjectOpenHashSet<E2Multiblock>()

		var consumed = 0.0

		for (input in multiblockInputs) {
			val entity = input.holder
			if (entity !is E2Multiblock) continue
			if (checkedEntities.contains(entity)) continue
			checkedEntities.add(entity)

			consumed += entity.getTotalE2Consumption()
		}

		val availablePercentage = availablePower / consumed

		for (entity in checkedEntities) {
			entity.markPowerShortage(availablePercentage)
		}

		return availablePercentage
	}

	override fun save(adapterContext: PersistentDataAdapterContext): PersistentDataContainer {
		return adapterContext.newPersistentDataContainer()
	}

	/**
	 * Returns a pair of a location map of inputs, and a location map of outputs
	 **/
	private fun trackIO(): Pair<ObjectOpenHashSet<IOPort.RegisteredMetaDataInput<E2PortMetaData>>, ObjectOpenHashSet<IOPort.RegisteredMetaDataInput<E2PortMetaData>>> {
		val inputs = ObjectOpenHashSet<IOPort.RegisteredMetaDataInput<E2PortMetaData>>()
		val outputs = ObjectOpenHashSet<IOPort.RegisteredMetaDataInput<E2PortMetaData>>()

		for (node in getGraphNodes()) {
			val ports: ObjectOpenHashSet<IOPort.RegisteredMetaDataInput<E2PortMetaData>> = manager.transportManager.getInputProvider().getPorts(IOType.E2, node.location)

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
