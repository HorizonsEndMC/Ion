package net.horizonsend.ion.server.features.transport.manager.graph.fluid

import net.horizonsend.ion.server.core.registration.keys.CustomBlockKeys
import net.horizonsend.ion.server.core.registration.keys.TransportNetworkNodeTypeKeys
import net.horizonsend.ion.server.core.registration.registries.CustomBlockRegistry.Companion.customBlock
import net.horizonsend.ion.server.features.transport.fluids.FluidStack
import net.horizonsend.ion.server.features.transport.manager.graph.TransportNetwork
import net.horizonsend.ion.server.features.transport.manager.graph.TransportNodeType
import net.horizonsend.ion.server.features.transport.nodes.graph.TransportNode
import net.horizonsend.ion.server.miscellaneous.utils.ADJACENT_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getY
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import net.horizonsend.ion.server.miscellaneous.utils.faces
import net.horizonsend.ion.server.miscellaneous.utils.getBlockIfLoaded
import org.bukkit.Axis
import org.bukkit.block.BlockFace

abstract class FluidNode(location: BlockKey, type: TransportNodeType<*>, val volume: Double) : TransportNode(location, type) {
	private lateinit var graph: FluidNetwork

	abstract val flowCapacity: Double

	override fun getNetwork(): TransportNetwork<*> = graph
	override fun setNetworkOwner(graph: TransportNetwork<*>) {
		this.graph = graph as FluidNetwork
	}

	fun populateContents() {
		val networkVolume = graph.getVolume()
		val contribution = volume / networkVolume
		contents = graph.networkContents.asAmount(graph.networkContents.amount * contribution)
	}

	var contents = FluidStack.empty(); private set

	fun loadContents(newContents: FluidStack) {
		this.contents = newContents
	}

	override fun onLoadedIntoNetwork(network: TransportNetwork<*>) {
		network as FluidNetwork

		network.networkContents.combine(contents, getCenter().toLocation(network.manager.transportManager.getWorld()))
		contents = FluidStack.empty()
	}

	sealed interface LeakablePipe {
		val leakRate: Double
	}

	class RegularJunctionPipe(location: BlockKey) : FluidNode(location, TransportNetworkNodeTypeKeys.FLUID_JUNCTION_REGULAR.getValue(), 10.0) {
		override val flowCapacity: Double = 10.0

		override fun isIntact(): Boolean? {
			val world = getNetwork().manager.transportManager.getWorld()
			val globalVec3i = getNetwork().manager.transportManager.getGlobalCoordinate(toVec3i(location))
			val block = getBlockIfLoaded(world, globalVec3i.x, globalVec3i.y, globalVec3i.z) ?: return null

			return block.blockData.customBlock?.key == CustomBlockKeys.FLUID_PIPE_JUNCTION
		}

		override fun getPipableDirections(): Set<BlockFace> = ADJACENT_BLOCK_FACES
	}

	class RegularLinearPipe(location: BlockKey, val axis: Axis) : FluidNode(location, TransportNetworkNodeTypeKeys.FLUID_LINEAR_REGULAR.getValue(), 5.0), LeakablePipe {
		override val flowCapacity: Double = 5.0

		override val leakRate: Double = 1.0

		override fun isIntact(): Boolean? {
			val world = getNetwork().manager.transportManager.getWorld()
			val globalVec3i = getNetwork().manager.transportManager.getGlobalCoordinate(toVec3i(location))
			val block = getBlockIfLoaded(world, globalVec3i.x, globalVec3i.y, globalVec3i.z) ?: return null

			return block.blockData.customBlock?.key == CustomBlockKeys.FLUID_PIPE
		}

		override fun getPipableDirections(): Set<BlockFace> = setOf(axis.faces.first, axis.faces.second)
	}

	class ReinforcedJunctionPipe(location: BlockKey) : FluidNode(location, TransportNetworkNodeTypeKeys.FLUID_JUNCTION_REINFORCED.getValue(), 10.0) {
		override val flowCapacity: Double = 30.0

		override fun isIntact(): Boolean? {
			val world = getNetwork().manager.transportManager.getWorld()
			val globalVec3i = getNetwork().manager.transportManager.getGlobalCoordinate(toVec3i(location))
			val block = getBlockIfLoaded(world, globalVec3i.x, globalVec3i.y, globalVec3i.z) ?: return null

			return block.blockData.customBlock?.key == CustomBlockKeys.REINFORCED_FLUID_PIPE_JUNCTION
		}

		override fun getPipableDirections(): Set<BlockFace> = ADJACENT_BLOCK_FACES
	}

	class ReinforcedLinearPipe(location: BlockKey, val axis: Axis) : FluidNode(location, TransportNetworkNodeTypeKeys.FLUID_LINEAR_REINFORCED.getValue(), 5.0), LeakablePipe {
		override val flowCapacity: Double = 15.0

		override val leakRate: Double = 1.0

		override fun isIntact(): Boolean? {
			val world = getNetwork().manager.transportManager.getWorld()
			val globalVec3i = getNetwork().manager.transportManager.getGlobalCoordinate(toVec3i(location))
			val block = getBlockIfLoaded(world, globalVec3i.x, globalVec3i.y, globalVec3i.z) ?: return null

			return block.blockData.customBlock?.key == CustomBlockKeys.REINFORCED_FLUID_PIPE
		}

		override fun getPipableDirections(): Set<BlockFace> = setOf(axis.faces.first, axis.faces.second)
	}

	class FluidPort(location: BlockKey) : FluidNode(location, TransportNetworkNodeTypeKeys.FLUID_PORT.getValue(), 0.0) {
		override val flowCapacity = 50.0

		val removalCapacity: Double get() = 50.0
		val additionCapacity: Double get() = 5.0

		override fun isIntact(): Boolean? {
			val world = getNetwork().manager.transportManager.getWorld()
			val globalVec3i = getNetwork().manager.transportManager.getGlobalCoordinate(toVec3i(location))
			val block = getBlockIfLoaded(world, globalVec3i.x, globalVec3i.y, globalVec3i.z) ?: return null

			return block.blockData.customBlock?.key == CustomBlockKeys.FLUID_PORT
		}

		override fun getPipableDirections(): Set<BlockFace> = ADJACENT_BLOCK_FACES
	}

	class FluidValve(location: BlockKey) : FluidNode(location, TransportNetworkNodeTypeKeys.FLUID_VALVE.getValue(), 0.0) {
		override val flowCapacity: Double get() {
			val block = getBlockIfLoaded(getNetwork().manager.transportManager.getWorld(), getX(location), getY(location), getZ(location)) ?:  return 0.0
			return if (block.isBlockPowered) Double.MAX_VALUE else 0.0
		}

		override fun isIntact(): Boolean? {
			val world = getNetwork().manager.transportManager.getWorld()
			val globalVec3i = getNetwork().manager.transportManager.getGlobalCoordinate(toVec3i(location))
			val block = getBlockIfLoaded(world, globalVec3i.x, globalVec3i.y, globalVec3i.z) ?: return null

			return block.blockData.customBlock?.key == CustomBlockKeys.FLUID_VALVE
		}

		override fun getPipableDirections(): Set<BlockFace> = ADJACENT_BLOCK_FACES
	}
}
