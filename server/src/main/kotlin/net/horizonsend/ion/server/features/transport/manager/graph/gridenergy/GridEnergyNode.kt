package net.horizonsend.ion.server.features.transport.manager.graph.gridenergy

import net.horizonsend.ion.server.core.registration.keys.CustomBlockKeys
import net.horizonsend.ion.server.core.registration.keys.TransportNetworkNodeTypeKeys
import net.horizonsend.ion.server.core.registration.registries.CustomBlockRegistry.Companion.customBlock
import net.horizonsend.ion.server.features.multiblock.entity.type.gridenergy.GridEnergyMultiblock
import net.horizonsend.ion.server.features.transport.inputs.IOType
import net.horizonsend.ion.server.features.transport.manager.graph.FlowNode
import net.horizonsend.ion.server.features.transport.manager.graph.TransportNetwork
import net.horizonsend.ion.server.features.transport.manager.graph.TransportNodeType
import net.horizonsend.ion.server.miscellaneous.utils.ADJACENT_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.axis
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.faces
import org.bukkit.Axis
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Directional

abstract class GridEnergyNode(location: BlockKey, type: TransportNodeType<*>) : FlowNode(location, type) {
	private lateinit var graph: GridEnergyNetwork

	override fun getNetwork(): TransportNetwork<*> = graph
	override fun setNetworkOwner(graph: TransportNetwork<*>) {
		this.graph = graph as GridEnergyNetwork
	}

	class GridEnergyPortNode(location: BlockKey) : GridEnergyNode(location, TransportNetworkNodeTypeKeys.GRID_ENERGY_PORT.getValue()) {
		override val flowCapacity: Double get() = getIO(IOType.GRID_ENERGY).sumOf {
			if (it.metaData.inputAllowed) (it.holder as GridEnergyMultiblock).getTotalGridEnergyConsumption()
			else (it.holder as GridEnergyMultiblock).getGridEnergyOutput()
		}

		override fun isIntact(): Boolean? {
			val block = getBlock() ?: return null
			return block.blockData.customBlock?.key == CustomBlockKeys.GRID_ENERGY_PORT
		}

		override fun getPipableDirections(): Set<BlockFace> = ADJACENT_BLOCK_FACES
	}

	class GridEnergyJunctionNode(location: BlockKey) : GridEnergyNode(location, TransportNetworkNodeTypeKeys.GRID_ENERGY_SPONGE.getValue()) {
		override val flowCapacity: Double = 24000.0 // 24 kw

		override fun isIntact(): Boolean? {
			val block = getBlock() ?: return null
			return block.type == Material.SPONGE
		}

		override fun getPipableDirections(): Set<BlockFace> = ADJACENT_BLOCK_FACES
	}

	class GridEnergyLinearNode(location: BlockKey, val axis: Axis) : GridEnergyNode(location, TransportNetworkNodeTypeKeys.GRID_ENERGY_END_ROD.getValue()) {
		override val flowCapacity: Double = 24000.0 // 24 kw

		override fun isIntact(): Boolean? {
			val block = getBlock() ?: return null
			val data = block.blockData
			return data.material == Material.END_ROD && (data as Directional).facing.axis == axis
		}

		private val dirs = setOf(axis.faces.first, axis.faces.second)
		override fun getPipableDirections(): Set<BlockFace> = dirs
	}

	class GridEnergyCableJunction(location: BlockKey) : GridEnergyNode(location, TransportNetworkNodeTypeKeys.GRID_ENERGY_CABLE_JUNCTION.getValue()) {
		override val flowCapacity: Double = 120000.0 // 120 kw

		override fun isIntact(): Boolean? {
			val block = getBlock() ?: return null
			return block.customBlock?.key == CustomBlockKeys.GRID_ENERGY_CABLE_JUNCTION
		}

		override fun getPipableDirections(): Set<BlockFace> = ADJACENT_BLOCK_FACES
	}

	class GridEnergyCable(location: BlockKey, val axis: Axis) : GridEnergyNode(location, TransportNetworkNodeTypeKeys.GRID_ENERGY_CABLE.getValue()) {
		override val flowCapacity: Double = 24000.0 // 24 kw

		override fun isIntact(): Boolean? {
			val block = getBlock() ?: return null
			val data = block.blockData
			return data.customBlock?.key == CustomBlockKeys.GRID_ENERGY_CABLE_JUNCTION
		}

		private val dirs = setOf(axis.faces.first, axis.faces.second)
		override fun getPipableDirections(): Set<BlockFace> = dirs
	}

	class UltraHighCapacityGridEnergyJunctionNode(location: BlockKey) : GridEnergyNode(location, TransportNetworkNodeTypeKeys.ULTRA_HIGH_CAPACITY_GRID_ENERGY_JUNCTION.getValue()) {
		override val flowCapacity: Double = Double.MAX_VALUE // infinite

		override fun isIntact(): Boolean? {
			val block = getBlock() ?: return null
			return block.type == Material.NETHERITE_BLOCK // TODO temp
		}

		override fun getPipableDirections(): Set<BlockFace> = ADJACENT_BLOCK_FACES
	}
}
