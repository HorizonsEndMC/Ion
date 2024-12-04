package net.horizonsend.ion.server.features.transport.nodes.types

import net.horizonsend.ion.common.utils.miscellaneous.roundToHundredth
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlerHolder
import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers
import net.horizonsend.ion.server.features.client.display.modular.display.FlowMeterDisplay
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PoweredMultiblockEntity
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.horizonsend.ion.server.features.transport.nodes.cache.PowerTransportCache
import net.horizonsend.ion.server.features.transport.util.CacheType
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
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
		fun getPoweredEntities(world: World, location: BlockKey) = world.ion.inputManager.getHolders(CacheType.POWER, location).filterIsInstance<PoweredMultiblockEntity>()
	}

    data object PowerMergeNode : PowerNode {
        override val pathfindingResistance: Double = 0.5
        override fun canTransferFrom(other: Node, offset: BlockFace): Boolean = other is SpongeNode
        override fun canTransferTo(other: Node, offset: BlockFace): Boolean = other !is SpongeNode
        override fun getTransferableDirections(backwards: BlockFace): Set<BlockFace> = ADJACENT_BLOCK_FACES.minus(backwards)

		override fun filterPositionData(nextNodes: List<Node.NodePositionData>, backwards: BlockFace): List<Node.NodePositionData> {
			val forward = backwards.oppositeFace
			nextNodes.firstOrNull { it.offset == forward }?.let { return listOf(it) }
			return nextNodes
		}
    }

    data object PowerInvertedMergeNode : PowerNode {
        override val pathfindingResistance: Double = 0.5
        override fun canTransferFrom(other: Node, offset: BlockFace): Boolean = other is EndRodNode
        override fun canTransferTo(other: Node, offset: BlockFace): Boolean = other !is EndRodNode
        override fun getTransferableDirections(backwards: BlockFace): Set<BlockFace> = ADJACENT_BLOCK_FACES.minus(backwards)

		override fun filterPositionData(nextNodes: List<Node.NodePositionData>, backwards: BlockFace): List<Node.NodePositionData> {
			val forward = backwards.oppositeFace
			nextNodes.firstOrNull { it.offset == forward }?.let { return listOf(it) }
			return nextNodes
		}
    }

    data class PowerFlowMeter(val cache: PowerTransportCache, var face: BlockFace, val location: BlockKey) : PowerNode, ComplexNode, DisplayHandlerHolder {
		override var isAlive: Boolean = true

        val displayHandler = DisplayHandlers.newBlockOverlay(
			this,
            cache.holder.getWorld(),
            toVec3i(location),
            face,
            FlowMeterDisplay(this, 0.0, 0.0, 0.0, 0.7f)
        ).register()

        private val STORED_AVERAGES = 20
        private val averages = mutableListOf<TransferredPower>()

        fun onCompleteChain(transferred: Int) {
            addTransferred(TransferredPower(transferred, System.currentTimeMillis()))
            displayHandler.update()
        }

        private fun addTransferred(transferredSnapshot: TransferredPower) {
            val currentSize = averages.size

            if (currentSize < STORED_AVERAGES) {
                averages.add(transferredSnapshot)
                return
            }

            // If it is full, shift all averages to the right
            for (index in 18 downTo 0) {
                averages[index + 1] = averages[index]
            }

            averages[0] = transferredSnapshot
        }

        private fun calculateAverage(): Double {
            val sum = averages.sumOf { it.transferred }

            val timeDiff = (System.currentTimeMillis() - averages.minOf { it.time }) / 1000.0

            return sum / timeDiff
        }

        fun formatFlow(): Component {
            var avg = runCatching { calculateAverage().roundToHundredth() }.getOrDefault(0.0)

            // If no averages, or no power has been moved in 5 seconds, go to 0
            if (averages.isEmpty() || System.currentTimeMillis() - averages.maxOf { it.time } > 5000) {
                avg = 0.0
            }

            return ofChildren(FlowMeterDisplay.firstLine, Component.text(avg, NamedTextColor.GREEN), FlowMeterDisplay.secondLine)
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
            displayHandler.displace(movement)
        }

        private data class TransferredPower(val transferred: Int, val time: Long)
    }
}
