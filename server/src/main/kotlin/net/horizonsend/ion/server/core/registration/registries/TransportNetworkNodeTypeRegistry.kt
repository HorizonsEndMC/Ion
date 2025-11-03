package net.horizonsend.ion.server.core.registration.registries

import com.manya.pdc.base.EnumDataType
import net.horizonsend.ion.server.core.registration.keys.KeyRegistry
import net.horizonsend.ion.server.core.registration.keys.RegistryKeys
import net.horizonsend.ion.server.core.registration.keys.TransportNetworkNodeTypeKeys
import net.horizonsend.ion.server.core.registration.keys.TransportNetworkNodeTypeKeys.FLUID_JUNCTION_REGULAR
import net.horizonsend.ion.server.core.registration.keys.TransportNetworkNodeTypeKeys.FLUID_JUNCTION_REINFORCED
import net.horizonsend.ion.server.core.registration.keys.TransportNetworkNodeTypeKeys.FLUID_LINEAR_REGULAR
import net.horizonsend.ion.server.core.registration.keys.TransportNetworkNodeTypeKeys.FLUID_LINEAR_REINFORCED
import net.horizonsend.ion.server.core.registration.keys.TransportNetworkNodeTypeKeys.FLUID_PORT
import net.horizonsend.ion.server.core.registration.keys.TransportNetworkNodeTypeKeys.FLUID_VALVE
import net.horizonsend.ion.server.core.registration.keys.TransportNetworkNodeTypeKeys.GRID_ENERGY_CABLE_JUNCTION
import net.horizonsend.ion.server.core.registration.keys.TransportNetworkNodeTypeKeys.GRID_ENERGY_JUNCTION
import net.horizonsend.ion.server.core.registration.keys.TransportNetworkNodeTypeKeys.GRID_ENERGY_LINEAR
import net.horizonsend.ion.server.core.registration.keys.TransportNetworkNodeTypeKeys.GRID_ENERGY_PORT
import net.horizonsend.ion.server.core.registration.keys.TransportNetworkNodeTypeKeys.TEMPERATURE_GAUGE
import net.horizonsend.ion.server.core.registration.keys.TransportNetworkNodeTypeKeys.ULTRA_HIGH_CAPACITY_GRID_ENERGY_JUNCTION
import net.horizonsend.ion.server.features.transport.fluids.FluidStack
import net.horizonsend.ion.server.features.transport.manager.graph.TransportNodeType
import net.horizonsend.ion.server.features.transport.manager.graph.fluid.FluidNode.FluidPort
import net.horizonsend.ion.server.features.transport.manager.graph.fluid.FluidNode.FluidValve
import net.horizonsend.ion.server.features.transport.manager.graph.fluid.FluidNode.RegularJunctionPipe
import net.horizonsend.ion.server.features.transport.manager.graph.fluid.FluidNode.RegularLinearPipe
import net.horizonsend.ion.server.features.transport.manager.graph.fluid.FluidNode.ReinforcedJunctionPipe
import net.horizonsend.ion.server.features.transport.manager.graph.fluid.FluidNode.ReinforcedLinearPipe
import net.horizonsend.ion.server.features.transport.manager.graph.fluid.FluidNode.TemperatureGauge
import net.horizonsend.ion.server.features.transport.manager.graph.gridenergy.GridEnergyNode.GridEnergyCableJunction
import net.horizonsend.ion.server.features.transport.manager.graph.gridenergy.GridEnergyNode.GridEnergyJunctionNode
import net.horizonsend.ion.server.features.transport.manager.graph.gridenergy.GridEnergyNode.GridEnergyLinearNode
import net.horizonsend.ion.server.features.transport.manager.graph.gridenergy.GridEnergyNode.GridEnergyPortNode
import net.horizonsend.ion.server.features.transport.manager.graph.gridenergy.GridEnergyNode.UltraHighCapacityGridEnergyJunctionNode
import net.horizonsend.ion.server.features.transport.nodes.graph.TransportNode.Companion.NODE_POSITION
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.CONTENTS
import org.bukkit.Axis
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

class TransportNetworkNodeTypeRegistry : Registry<TransportNodeType<*>>(RegistryKeys.TRANSPORT_NETWORK_NODE_TYPE) {
	override fun getKeySet(): KeyRegistry<TransportNodeType<*>> = TransportNetworkNodeTypeKeys

	override fun boostrap() {
		register(FLUID_PORT, object : TransportNodeType<FluidPort>(FLUID_PORT) {
			override fun deserialize(data: PersistentDataContainer, adapterContext: PersistentDataAdapterContext): FluidPort {
				val node = FluidPort(data.get(NODE_POSITION, PersistentDataType.LONG)!!)
				return node
			}

			override fun serializeData(node: FluidPort, adapterContext: PersistentDataAdapterContext): PersistentDataContainer {
				val pdc = adapterContext.newPersistentDataContainer()
				pdc.set(NODE_POSITION, PersistentDataType.LONG, node.location)
				return pdc
			}
		})
		register(FLUID_VALVE, object : TransportNodeType<FluidValve>(FLUID_VALVE) {
			override fun deserialize(data: PersistentDataContainer, adapterContext: PersistentDataAdapterContext): FluidValve {
				val node = FluidValve(data.get(NODE_POSITION, PersistentDataType.LONG)!!)
				return node
			}

			override fun serializeData(node: FluidValve, adapterContext: PersistentDataAdapterContext): PersistentDataContainer {
				val pdc = adapterContext.newPersistentDataContainer()
				pdc.set(NODE_POSITION, PersistentDataType.LONG, node.location)
				return pdc
			}
		})
		register(TEMPERATURE_GAUGE, object : TransportNodeType<TemperatureGauge>(TEMPERATURE_GAUGE) {
			override fun deserialize(data: PersistentDataContainer, adapterContext: PersistentDataAdapterContext): TemperatureGauge {
				val node = TemperatureGauge(data.get(NODE_POSITION, PersistentDataType.LONG)!!)
				return node
			}

			override fun serializeData(node: TemperatureGauge, adapterContext: PersistentDataAdapterContext): PersistentDataContainer {
				val pdc = adapterContext.newPersistentDataContainer()
				pdc.set(NODE_POSITION, PersistentDataType.LONG, node.location)
				return pdc
			}
		})
		register(FLUID_JUNCTION_REGULAR, object : TransportNodeType<RegularJunctionPipe>(FLUID_JUNCTION_REGULAR) {
			override fun deserialize(data: PersistentDataContainer, adapterContext: PersistentDataAdapterContext): RegularJunctionPipe {
				val node = RegularJunctionPipe(data.get(NODE_POSITION, PersistentDataType.LONG)!!)
				node.loadContents(data.get(CONTENTS, FluidStack)!!)
				return node
			}

			override fun serializeData(node: RegularJunctionPipe, adapterContext: PersistentDataAdapterContext): PersistentDataContainer {
				val pdc = adapterContext.newPersistentDataContainer()
				pdc.set(NODE_POSITION, PersistentDataType.LONG, node.location)
				pdc.set(CONTENTS, FluidStack, node.contents)
				return pdc
			}
		})
		register(FLUID_LINEAR_REGULAR, object : TransportNodeType<RegularLinearPipe>(FLUID_LINEAR_REGULAR) {
			override fun deserialize(data: PersistentDataContainer, adapterContext: PersistentDataAdapterContext): RegularLinearPipe {
				val node = RegularLinearPipe(data.get(NODE_POSITION, PersistentDataType.LONG)!!, data.get(NamespacedKeys.AXIS, axisType)!!)
				node.loadContents(data.get(CONTENTS, FluidStack)!!)
				return node
			}

			override fun serializeData(node: RegularLinearPipe, adapterContext: PersistentDataAdapterContext): PersistentDataContainer {
				val pdc = adapterContext.newPersistentDataContainer()
				pdc.set(NODE_POSITION, PersistentDataType.LONG, node.location)
				pdc.set(CONTENTS, FluidStack, node.contents)
				pdc.set(NamespacedKeys.AXIS, axisType, node.axis)
				return pdc
			}
		})
		register(FLUID_JUNCTION_REINFORCED, object : TransportNodeType<ReinforcedJunctionPipe>(FLUID_JUNCTION_REINFORCED) {
			override fun deserialize(data: PersistentDataContainer, adapterContext: PersistentDataAdapterContext): ReinforcedJunctionPipe {
				val node = ReinforcedJunctionPipe(data.get(NODE_POSITION, PersistentDataType.LONG)!!)
				node.loadContents(data.get(CONTENTS, FluidStack)!!)
				return node
			}

			override fun serializeData(node: ReinforcedJunctionPipe, adapterContext: PersistentDataAdapterContext): PersistentDataContainer {
				val pdc = adapterContext.newPersistentDataContainer()
				pdc.set(NODE_POSITION, PersistentDataType.LONG, node.location)
				pdc.set(CONTENTS, FluidStack, node.contents)
				return pdc
			}
		})
		register(FLUID_LINEAR_REINFORCED, object : TransportNodeType<ReinforcedLinearPipe>(FLUID_LINEAR_REINFORCED) {
			override fun deserialize(data: PersistentDataContainer, adapterContext: PersistentDataAdapterContext): ReinforcedLinearPipe {
				val node = ReinforcedLinearPipe(data.get(NODE_POSITION, PersistentDataType.LONG)!!, data.get(NamespacedKeys.AXIS, axisType)!!)
				node.loadContents(data.get(CONTENTS, FluidStack)!!)
				return node
			}

			override fun serializeData(node: ReinforcedLinearPipe, adapterContext: PersistentDataAdapterContext): PersistentDataContainer {
				val pdc = adapterContext.newPersistentDataContainer()
				pdc.set(NODE_POSITION, PersistentDataType.LONG, node.location)
				pdc.set(CONTENTS, FluidStack, node.contents)
				pdc.set(NamespacedKeys.AXIS, axisType, node.axis)
				return pdc
			}
		})
		register(GRID_ENERGY_PORT, object : TransportNodeType<GridEnergyPortNode>(GRID_ENERGY_PORT) {
			override fun deserialize(data: PersistentDataContainer, adapterContext: PersistentDataAdapterContext): GridEnergyPortNode {
				val node = GridEnergyPortNode(data.get(NODE_POSITION, PersistentDataType.LONG)!!)
				return node
			}

			override fun serializeData(node: GridEnergyPortNode, adapterContext: PersistentDataAdapterContext): PersistentDataContainer {
				val pdc = adapterContext.newPersistentDataContainer()
				pdc.set(NODE_POSITION, PersistentDataType.LONG, node.location)
				return pdc
			}
		})
		register(GRID_ENERGY_JUNCTION, object : TransportNodeType<GridEnergyJunctionNode>(GRID_ENERGY_JUNCTION) {
			override fun deserialize(data: PersistentDataContainer, adapterContext: PersistentDataAdapterContext): GridEnergyJunctionNode {
				val node = GridEnergyJunctionNode(data.get(NODE_POSITION, PersistentDataType.LONG)!!)
				return node
			}

			override fun serializeData(node: GridEnergyJunctionNode, adapterContext: PersistentDataAdapterContext): PersistentDataContainer {
				val pdc = adapterContext.newPersistentDataContainer()
				pdc.set(NODE_POSITION, PersistentDataType.LONG, node.location)
				return pdc
			}
		})
		register(GRID_ENERGY_LINEAR, object : TransportNodeType<GridEnergyLinearNode>(GRID_ENERGY_LINEAR) {
			override fun deserialize(data: PersistentDataContainer, adapterContext: PersistentDataAdapterContext): GridEnergyLinearNode {
				val node = GridEnergyLinearNode(data.get(NODE_POSITION, PersistentDataType.LONG)!!, data.get(NamespacedKeys.AXIS, axisType)!!)
				return node
			}

			override fun serializeData(node: GridEnergyLinearNode, adapterContext: PersistentDataAdapterContext): PersistentDataContainer {
				val pdc = adapterContext.newPersistentDataContainer()
				pdc.set(NODE_POSITION, PersistentDataType.LONG, node.location)
				pdc.set(NamespacedKeys.AXIS, axisType, node.axis)
				return pdc
			}
		})
		register(GRID_ENERGY_CABLE_JUNCTION, object : TransportNodeType<GridEnergyCableJunction>(GRID_ENERGY_CABLE_JUNCTION) {
			override fun deserialize(data: PersistentDataContainer, adapterContext: PersistentDataAdapterContext): GridEnergyCableJunction {
				val node = GridEnergyCableJunction(data.get(NODE_POSITION, PersistentDataType.LONG)!!)
				return node
			}

			override fun serializeData(node: GridEnergyCableJunction, adapterContext: PersistentDataAdapterContext): PersistentDataContainer {
				val pdc = adapterContext.newPersistentDataContainer()
				pdc.set(NODE_POSITION, PersistentDataType.LONG, node.location)
				return pdc
			}
		})
		register(ULTRA_HIGH_CAPACITY_GRID_ENERGY_JUNCTION, object : TransportNodeType<UltraHighCapacityGridEnergyJunctionNode>(ULTRA_HIGH_CAPACITY_GRID_ENERGY_JUNCTION) {
			override fun deserialize(data: PersistentDataContainer, adapterContext: PersistentDataAdapterContext): UltraHighCapacityGridEnergyJunctionNode {
				val node = UltraHighCapacityGridEnergyJunctionNode(data.get(NODE_POSITION, PersistentDataType.LONG)!!)
				return node
			}

			override fun serializeData(node: UltraHighCapacityGridEnergyJunctionNode, adapterContext: PersistentDataAdapterContext): PersistentDataContainer {
				val pdc = adapterContext.newPersistentDataContainer()
				pdc.set(NODE_POSITION, PersistentDataType.LONG, node.location)
				return pdc
			}
		})
	}

	private companion object {
		val axisType = EnumDataType(Axis::class.java)
	}
}
