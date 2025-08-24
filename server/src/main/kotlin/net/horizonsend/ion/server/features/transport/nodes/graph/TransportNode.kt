package net.horizonsend.ion.server.features.transport.nodes.graph

import net.horizonsend.ion.server.features.transport.fluids.FluidStack
import net.horizonsend.ion.server.features.transport.manager.graph.TransportNetwork
import net.horizonsend.ion.server.features.transport.manager.graph.fluid.FluidNode
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import org.bukkit.block.BlockFace
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

interface TransportNode {
	val location: BlockKey

	fun getCenter() = toVec3i(location).toCenterVector()

	/**
	 * Returns if it is intact, null if it cannot be determined
	 **/
	fun isIntact(): Boolean?

	fun setNetworkOwner(graph: TransportNetwork<*>)
	fun getNetwork(): TransportNetwork<*>

	fun getPersistentDataType(): NodePersistentDataType<*>

	fun getPipableDirections(): Set<BlockFace>

	companion object {
		val NODE_POSITION = NamespacedKeys.key("node_position")
	}

	class NodePersistentDataType<T : TransportNode>(
		val clazz: KClass<T>,
		val additionalDataStore: PersistentDataContainer.(T) -> Unit,
		val newInstance: (PersistentDataContainer, PersistentDataAdapterContext) -> T,
	) : PersistentDataType<PersistentDataContainer, T> {
		override fun getPrimitiveType(): Class<PersistentDataContainer> = PersistentDataContainer::class.java
		override fun getComplexType(): Class<T> = clazz.java

		override fun toPrimitive(complex: T, context: PersistentDataAdapterContext): PersistentDataContainer {
			val pdc = context.newPersistentDataContainer()

			pdc.set(NODE_POSITION, PersistentDataType.LONG, complex.location)

			additionalDataStore.invoke(pdc, complex)

			return pdc
		}

		override fun fromPrimitive(primitive: PersistentDataContainer, context: PersistentDataAdapterContext): T {
			return newInstance.invoke(primitive, context)
		}

		companion object {
			inline fun <reified T : FluidNode> simpleFluid() : NodePersistentDataType<T> = NodePersistentDataType(
					T::class,
					{ set(NamespacedKeys.CONTENTS, FluidStack, it.contents) },
					{ data, context -> T::class.primaryConstructor!!.call(data.get(NODE_POSITION, PersistentDataType.LONG)).apply { loadContents(data, context) } }
				)

			inline fun <reified T : TransportNode> simple() : NodePersistentDataType<T> = NodePersistentDataType(T::class, {}, { data, context ->  T::class.primaryConstructor!!.call(data.get(NODE_POSITION, PersistentDataType.LONG)) })
		}
	}
}
