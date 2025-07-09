package net.horizonsend.ion.server.features.transport.manager.graph.fluid

import com.manya.pdc.base.EnumDataType
import net.horizonsend.ion.server.core.registration.keys.CustomBlockKeys
import net.horizonsend.ion.server.core.registration.registries.CustomBlockRegistry.Companion.customBlock
import net.horizonsend.ion.server.features.transport.fluids.FluidStack
import net.horizonsend.ion.server.features.transport.manager.graph.TransportNetwork
import net.horizonsend.ion.server.features.transport.nodes.graph.TransportNode
import net.horizonsend.ion.server.features.transport.nodes.graph.TransportNode.Companion.NODE_POSITION
import net.horizonsend.ion.server.features.transport.nodes.graph.TransportNode.NodePersistentDataType
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.ADJACENT_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import net.horizonsend.ion.server.miscellaneous.utils.faces
import net.horizonsend.ion.server.miscellaneous.utils.getBlockIfLoaded
import org.bukkit.Axis
import org.bukkit.block.BlockFace
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

abstract class FluidNode(val volume: Double) : TransportNode {
	private lateinit var graph: FluidNetwork

	abstract val flowCapacity: Double

	override fun getNetwork(): TransportNetwork<*> = graph
	override fun setNetworkOwner(graph: TransportNetwork<*>) {
		this.graph = graph as FluidNetwork
	}

	val contents = FluidStack.empty()

	fun loadContents(saved: PersistentDataContainer) {

	}



	class RegularJunctionPipe(override val location: BlockKey) : FluidNode(10.0) {
		override val flowCapacity: Double = 10.0

		override fun isIntact(): Boolean? {
			val world = getNetwork().manager.transportManager.getWorld()
			val globalVec3i = getNetwork().manager.transportManager.getGlobalCoordinate(toVec3i(location))
			val block = getBlockIfLoaded(world, globalVec3i.x, globalVec3i.y, globalVec3i.z) ?: return null

			return block.blockData.customBlock?.key == CustomBlockKeys.FLUID_PIPE_JUNCTION
		}

		override fun getPipableDirections(): Set<BlockFace> = ADJACENT_BLOCK_FACES

		override fun getPersistentDataType(): TransportNode.NodePersistentDataType<*> = persistentDataType
		private companion object { val persistentDataType = TransportNode.NodePersistentDataType.simpleFluid<RegularJunctionPipe>() }
	}

	sealed interface LeakablePipe {
		val leakRate: Double
	}

	class RegularLinearPipe(override val location: BlockKey, val axis: Axis) : FluidNode(5.0), LeakablePipe {
		override val flowCapacity: Double = 5.0

		override val leakRate: Double = 1.0

		override fun isIntact(): Boolean? {
			val world = getNetwork().manager.transportManager.getWorld()
			val globalVec3i = getNetwork().manager.transportManager.getGlobalCoordinate(toVec3i(location))
			val block = getBlockIfLoaded(world, globalVec3i.x, globalVec3i.y, globalVec3i.z) ?: return null

			return block.blockData.customBlock?.key == CustomBlockKeys.FLUID_PIPE
		}

		override fun getPipableDirections(): Set<BlockFace> = setOf(axis.faces.first, axis.faces.second)

		override fun getPersistentDataType(): NodePersistentDataType<*> = persistentDataType
		private companion object {
			val axisType = EnumDataType(Axis::class.java)

			val persistentDataType = NodePersistentDataType(
				RegularLinearPipe::class,
				{
					set(NamespacedKeys.CONTENTS, FluidStack, it.contents)
					set(NamespacedKeys.AXIS, axisType, it.axis)
				},
				{ RegularLinearPipe(it.get(NODE_POSITION, PersistentDataType.LONG)!!, it.get(NamespacedKeys.AXIS, axisType)!!).apply { loadContents(it) } }
			)
		}
	}

	class Input(override val location: BlockKey) : FluidNode(0.0) {
		override val flowCapacity = 50.0

		override fun isIntact(): Boolean? {
			val world = getNetwork().manager.transportManager.getWorld()
			val globalVec3i = getNetwork().manager.transportManager.getGlobalCoordinate(toVec3i(location))
			val block = getBlockIfLoaded(world, globalVec3i.x, globalVec3i.y, globalVec3i.z) ?: return null

			return block.blockData.customBlock?.key == CustomBlockKeys.FLUID_INPUT
		}

		override fun getPipableDirections(): Set<BlockFace> = ADJACENT_BLOCK_FACES

		override fun getPersistentDataType(): TransportNode.NodePersistentDataType<*> = persistentDataType
		private companion object { val persistentDataType = TransportNode.NodePersistentDataType.simpleFluid<Input>() }
	}
}
