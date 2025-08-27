package net.horizonsend.ion.server.core.registration.registries

import com.manya.pdc.base.EnumDataType
import net.horizonsend.ion.server.core.registration.keys.KeyRegistry
import net.horizonsend.ion.server.core.registration.keys.RegistryKeys
import net.horizonsend.ion.server.core.registration.keys.TransportNetworkNodeTypeKeys
import net.horizonsend.ion.server.core.registration.keys.TransportNetworkNodeTypeKeys.FLUID_JUNCTION_REGULAR
import net.horizonsend.ion.server.core.registration.keys.TransportNetworkNodeTypeKeys.FLUID_LINEAR_REGULAR
import net.horizonsend.ion.server.core.registration.keys.TransportNetworkNodeTypeKeys.FLUID_PORT
import net.horizonsend.ion.server.features.transport.fluids.FluidStack
import net.horizonsend.ion.server.features.transport.manager.graph.TransportNodeType
import net.horizonsend.ion.server.features.transport.manager.graph.fluid.FluidNode.FluidPort
import net.horizonsend.ion.server.features.transport.manager.graph.fluid.FluidNode.RegularJunctionPipe
import net.horizonsend.ion.server.features.transport.manager.graph.fluid.FluidNode.RegularLinearPipe
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
		register(FLUID_JUNCTION_REGULAR, object : TransportNodeType<RegularJunctionPipe>(FLUID_JUNCTION_REGULAR) {
			override fun deserialize(data: PersistentDataContainer, adapterContext: PersistentDataAdapterContext): RegularJunctionPipe {
				val node = RegularJunctionPipe(data.get(NODE_POSITION, PersistentDataType.LONG)!!)
				node.loadContents(data, adapterContext)
				return node
			}

			override fun serializeData(complex: RegularJunctionPipe, adapterContext: PersistentDataAdapterContext): PersistentDataContainer {
				val pdc = adapterContext.newPersistentDataContainer()
				pdc.set(NODE_POSITION, PersistentDataType.LONG, complex.location)
				pdc.set(CONTENTS, FluidStack, complex.contents)
				return pdc
			}
		})
		register(FLUID_LINEAR_REGULAR, object : TransportNodeType<RegularLinearPipe>(FLUID_LINEAR_REGULAR) {
			override fun deserialize(data: PersistentDataContainer, adapterContext: PersistentDataAdapterContext): RegularLinearPipe {
				val node = RegularLinearPipe(data.get(NODE_POSITION, PersistentDataType.LONG)!!, data.get(NamespacedKeys.AXIS, axisType)!!)
				node.loadContents(data, adapterContext)
				return node
			}

			override fun serializeData(complex: RegularLinearPipe, adapterContext: PersistentDataAdapterContext): PersistentDataContainer {
				val pdc = adapterContext.newPersistentDataContainer()
				pdc.set(NODE_POSITION, PersistentDataType.LONG, complex.location)
				pdc.set(CONTENTS, FluidStack, complex.contents)
				pdc.set(NamespacedKeys.AXIS, axisType, complex.axis)
				return pdc
			}
		})
		register(FLUID_PORT, object : TransportNodeType<FluidPort>(FLUID_PORT) {
			override fun deserialize(data: PersistentDataContainer, adapterContext: PersistentDataAdapterContext): FluidPort {
				val node = FluidPort(data.get(NODE_POSITION, PersistentDataType.LONG)!!)
				return node
			}

			override fun serializeData(complex: FluidPort, adapterContext: PersistentDataAdapterContext): PersistentDataContainer {
				val pdc = adapterContext.newPersistentDataContainer()
				pdc.set(NODE_POSITION, PersistentDataType.LONG, complex.location)
				return pdc
			}
		})
	}

	private companion object {
		val axisType = EnumDataType(Axis::class.java)
	}
}
