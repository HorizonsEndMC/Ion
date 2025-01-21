package net.horizonsend.ion.server.features.transport.nodes.types

import net.horizonsend.ion.common.utils.miscellaneous.roundToHundredth
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlerHolder
import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers
import net.horizonsend.ion.server.features.client.display.modular.display.FlowMeterDisplayModule
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.horizonsend.ion.server.features.transport.nodes.cache.PowerTransportCache
import net.horizonsend.ion.server.features.transport.nodes.types.Node.NodePositionData
import net.horizonsend.ion.server.features.transport.util.CacheType
import net.horizonsend.ion.server.miscellaneous.utils.ADJACENT_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.axis
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import net.horizonsend.ion.server.miscellaneous.utils.faces
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Axis
import org.bukkit.World
import org.bukkit.block.BlockFace

sealed interface PowerNode : Node {
    override val cacheType: CacheType get() = CacheType.POWER

    data object SpongeNode : PowerNode {
        override val pathfindingResistance: Double = 1.0
        override fun canTransferFrom(other: Node, offset: BlockFace): Boolean = true
        override fun canTransferTo(other: Node, offset: BlockFace): Boolean = true
        override fun getTransferableDirections(backwards: BlockFace): Set<BlockFace> = ADJACENT_BLOCK_FACES.minus(backwards)
    }

    data class EndRodNode(var axis: Axis) : PowerNode, ComplexNode {
        override val pathfindingResistance: Double = 0.5
        override fun canTransferFrom(other: Node, offset: BlockFace): Boolean = offset.axis == this.axis
        override fun canTransferTo(other: Node, offset: BlockFace): Boolean = offset.axis == this.axis
        override fun getTransferableDirections(backwards: BlockFace): Set<BlockFace> = setOf(backwards.oppositeFace)
        override fun displace(movement: StarshipMovement) {
            this.axis = movement.displaceFace(this.axis.faces.first).axis
        }
    }

    data object PowerExtractorNode : PowerNode {
        override val pathfindingResistance: Double = 0.5
        override fun canTransferFrom(other: Node, offset: BlockFace): Boolean = false
        override fun canTransferTo(other: Node, offset: BlockFace): Boolean = other !is PowerInputNode
        override fun getTransferableDirections(backwards: BlockFace): Set<BlockFace> = ADJACENT_BLOCK_FACES.minus(backwards)
    }

	data object PowerInputNode : PowerNode {
		override val pathfindingResistance: Double = 0.0
		override fun canTransferFrom(other: Node, offset: BlockFace): Boolean = true
		override fun canTransferTo(other: Node, offset: BlockFace): Boolean = false
		override fun getTransferableDirections(backwards: BlockFace): Set<BlockFace> = ADJACENT_BLOCK_FACES.minus(backwards)
	}

	interface MergeNode : PowerNode {
		override fun canTransferFrom(other: Node, offset: BlockFace): Boolean = true
		override fun getTransferableDirections(backwards: BlockFace): Set<BlockFace> = ADJACENT_BLOCK_FACES.minus(backwards)

		override fun filterPositionData(nextNodes: List<NodePositionData>, backwards: BlockFace): List<NodePositionData> {
			val forward = backwards.oppositeFace

			val filtered = mutableListOf<NodePositionData>()
			for (node in nextNodes) {
				if (node.offset == forward) filtered.add(node)
				if (node.type is PowerInputNode) filtered.add(node)
			}

			if (filtered.isNotEmpty()) return filtered

			return nextNodes
		}

		/** Check common to all merge nodes, stops transfer between different types thereof */
		fun mergeNodeTransferCheck(other: Node) = if (other is MergeNode) other.javaClass == javaClass else true
	}

    sealed interface StandardMergeNode : MergeNode {
        override fun canTransferTo(other: Node, offset: BlockFace): Boolean = other !is SpongeNode && mergeNodeTransferCheck(other)
    }

	data object RedstoneMergeNode : StandardMergeNode {
		override val pathfindingResistance: Double = 0.5
	}

	data object IronMergeNode : StandardMergeNode {
		override val pathfindingResistance: Double = 0.5
	}

    data object InvertedMergeNode : PowerNode, MergeNode {
		override fun canTransferTo(other: Node, offset: BlockFace): Boolean = other !is EndRodNode && mergeNodeTransferCheck(other)
		override val pathfindingResistance: Double = 0.5
    }

    data class PowerFlowMeter(val cache: PowerTransportCache, var face: BlockFace, var world: World, var location: BlockKey) : PowerNode, ComplexNode, DisplayHandlerHolder {
		/** Data entry of transferred power, contains the amount and the timestamp if the transfer */
		private data class TransferredPower(val transferred: Int, val time: Long = System.currentTimeMillis())

		// Use array deque as a stack
		private val averages = ArrayDeque<TransferredPower?>(NUMBER_STORED_AVERAGES)

		override var isAlive: Boolean = true

        val displayHandler = DisplayHandlers.newBlockOverlay(this, toVec3i(location), face, { FlowMeterDisplayModule(it, this, 0.0, 0.0, 0.0, 0.7f) })

        fun onCompleteChain(transferred: Int) {
			// Push onto queue
			if (averages.size == NUMBER_STORED_AVERAGES) averages.removeFirst()
			averages.addLast(TransferredPower(transferred))

            displayHandler.update()
        }

        private fun calculateAverage(): Double {
			val nonNull = averages.filterNotNull()
            val sum = nonNull.sumOf { it.transferred }

            val timeDiff = (System.currentTimeMillis() - nonNull.minOf { it.time }) / 1000.0

            return sum / timeDiff
        }

        fun formatFlow(): Component {
			val nonNull = averages.filterNotNull()
            var avg = runCatching { calculateAverage().roundToHundredth() }.getOrDefault(0.0)

            // If no averages, or no power has been moved in 5 seconds, go to 0
            if (averages.isEmpty() || System.currentTimeMillis() - nonNull.maxOf { it.time } > 5000) {
                avg = 0.0
            }

            return ofChildren(FlowMeterDisplayModule.firstLine, Component.text(avg, NamedTextColor.GREEN), FlowMeterDisplayModule.secondLine)
        }

        override val pathfindingResistance: Double = 0.5
        override fun canTransferFrom(other: Node, offset: BlockFace): Boolean = true
        override fun canTransferTo(other: Node, offset: BlockFace): Boolean = true
        override fun getTransferableDirections(backwards: BlockFace): Set<BlockFace> = ADJACENT_BLOCK_FACES.minus(backwards)

        override fun onInvalidate() {
			isAlive = false
            displayHandler.remove()
        }

        override fun displace(movement: StarshipMovement) {
            this.face = movement.displaceFace(this.face)
			location = movement.displaceKey(location)

            displayHandler.displace(movement)
        }

		override fun handlerGetWorld(): World = world

		companion object { private const val NUMBER_STORED_AVERAGES = 20 }
	}
}
