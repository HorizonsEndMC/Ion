package net.horizonsend.ion.server.features.transport.nodes.types

import net.horizonsend.ion.common.utils.miscellaneous.roundToHundredth
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlerHolder
import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers
import net.horizonsend.ion.server.features.client.display.modular.display.FlowMeterDisplayModule
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.horizonsend.ion.server.features.transport.manager.ChunkTransportManager
import net.horizonsend.ion.server.features.transport.manager.TransportManager
import net.horizonsend.ion.server.features.transport.nodes.types.Node.Companion.adjacentMinusBackwards
import net.horizonsend.ion.server.features.transport.nodes.types.Node.NodePositionData
import net.horizonsend.ion.server.features.transport.util.CacheType
import net.horizonsend.ion.server.features.transport.util.RollingAverage
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.miscellaneous.utils.axis
import net.horizonsend.ion.server.miscellaneous.utils.axisOrNull
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import net.horizonsend.ion.server.miscellaneous.utils.faces
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
import org.bukkit.Axis
import org.bukkit.World
import org.bukkit.block.BlockFace
import java.text.DecimalFormat

sealed interface PowerNode : Node {
    override val cacheType: CacheType get() = CacheType.POWER
	override fun getMaxPathfinds(): Int = 1

    data object SpongeNode : PowerNode {
		override fun canTransferFrom(other: Node, offset: BlockFace): Boolean = true
        override fun canTransferTo(other: Node, offset: BlockFace): Boolean = true
        override fun getTransferableDirections(backwards: BlockFace): Set<BlockFace> = adjacentMinusBackwards(backwards)
		override fun getMaxPathfinds(): Int = 2
    }

    data class EndRodNode(var axis: Axis) : PowerNode, ComplexNode {
		override fun canTransferFrom(other: Node, offset: BlockFace): Boolean = offset.axisOrNull == this.axis
        override fun canTransferTo(other: Node, offset: BlockFace): Boolean = offset.axisOrNull == this.axis
        override fun getTransferableDirections(backwards: BlockFace): Set<BlockFace> = setOf(backwards.oppositeFace)
        override fun displace(movement: StarshipMovement) {
            this.axis = movement.displaceFace(this.axis.faces.first).axis
        }
    }

    data object PowerExtractorNode : PowerNode {
		override fun canTransferFrom(other: Node, offset: BlockFace): Boolean = false
        override fun canTransferTo(other: Node, offset: BlockFace): Boolean = other !is PowerInputNode
        override fun getTransferableDirections(backwards: BlockFace): Set<BlockFace> = adjacentMinusBackwards(backwards)
    }

	data object PowerInputNode : PowerNode {
		override fun canTransferFrom(other: Node, offset: BlockFace): Boolean = true
		override fun canTransferTo(other: Node, offset: BlockFace): Boolean = false
		override fun getTransferableDirections(backwards: BlockFace): Set<BlockFace> = setOf()
	}

	interface MergeNode : PowerNode {
		override fun getMaxPathfinds(): Int = 6
		override fun canTransferFrom(other: Node, offset: BlockFace): Boolean = true
		override fun getTransferableDirections(backwards: BlockFace): Set<BlockFace> = adjacentMinusBackwards(backwards)

		override fun filterPositionData(nextNodes: List<NodePositionData>, backwards: BlockFace): List<NodePositionData> {
			val forward = backwards.oppositeFace

			val filtered = mutableListOf<NodePositionData>()
			var forwardPresent = false
			for (node in nextNodes) {
				if (node.offset == forward) {
					forwardPresent = true
					filtered.add(node)
				}

				if (node.type is PowerInputNode) filtered.add(node)
			}

			if (filtered.isNotEmpty() && forwardPresent) return filtered

			return nextNodes
		}

		/** Check common to all merge nodes, stops transfer between different types thereof */
		fun mergeNodeTransferCheck(other: Node) = if (other is MergeNode) other.javaClass == javaClass else true
	}

    sealed interface StandardMergeNode : MergeNode {
        override fun canTransferTo(other: Node, offset: BlockFace): Boolean = other !is SpongeNode && mergeNodeTransferCheck(other)
    }

	data object RedstoneMergeNode : StandardMergeNode
	data object IronMergeNode : StandardMergeNode

    data object InvertedMergeNode : PowerNode, MergeNode {
		override fun canTransferTo(other: Node, offset: BlockFace): Boolean = other !is EndRodNode && mergeNodeTransferCheck(other)
	}

    data class PowerFlowMeter(var face: BlockFace, var world: World, val manager: TransportManager<*>, var location: BlockKey) : PowerNode, ComplexNode, DisplayHandlerHolder, TrackedNode {
		override fun canTransferFrom(other: Node, offset: BlockFace): Boolean = true
		override fun canTransferTo(other: Node, offset: BlockFace): Boolean = true
		override fun getTransferableDirections(backwards: BlockFace): Set<BlockFace> = adjacentMinusBackwards(backwards)

		override fun handlerGetWorld(): World = world

		private val rollingAverage = RollingAverage()

		override var isAlive: Boolean = true
		val displayHandler = DisplayHandlers.newBlockOverlay(
			this,
			toVec3i(location),
			face,
			{ FlowMeterDisplayModule(it, this, 0.0, 0.0, 0.0, 0.7f) }
		)
			.addKeepAlive {
				if (manager is ChunkTransportManager) {
					val chunk = world.ion.getChunk(getX(location).shr(4), getZ(location).shr(4)) ?: return@addKeepAlive false
					chunk.transportNetwork.powerNodeManager.cache.getCached(location) === this
				} else true
			}
			.register()

        fun onCompleteChain(transferred: Int) {
			rollingAverage.addEntry(transferred)

			displayHandler.update()
        }

        fun formatFlow(): Component {
            val avg = runCatching { rollingAverage.getAverage().roundToHundredth() }.getOrDefault(0.0)
            return ofChildren(firstLine, text(format.format(avg), GREEN),)
        }

        override fun onInvalidate() {
			isAlive = false
            displayHandler.remove()
        }

        override fun displace(movement: StarshipMovement) {
            this.face = movement.displaceFace(this.face)
			location = movement.displaceModernKey(location)
			movement.newWorld?.let { world = it }

            displayHandler.displace(movement)
        }

		private companion object {
			val firstLine = ofChildren(text("Δ", GREEN), text("E: ", YELLOW))
			val format = DecimalFormat("##.##")
		}
	}
}
