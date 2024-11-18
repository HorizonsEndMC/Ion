package net.horizonsend.ion.server.features.transport.nodes.types

import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.horizonsend.ion.server.features.transport.util.CacheType
import net.horizonsend.ion.server.miscellaneous.utils.ADJACENT_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.axis
import net.horizonsend.ion.server.miscellaneous.utils.faces
import org.bukkit.Axis
import org.bukkit.Material
import org.bukkit.block.BlockFace

sealed interface FluidNode : Node {
	override val cacheType: CacheType get() = CacheType.FLUID

	data class FluidJunctionNode(val channel: Material) : FluidNode {
		override val pathfindingResistance: Double = 1.0
		override fun canTransferFrom(other: Node, offset: BlockFace): Boolean = if (other is FluidJunctionNode) other.channel == channel else true
		override fun canTransferTo(other: Node, offset: BlockFace): Boolean = if (other is FluidJunctionNode) other.channel == channel else true
		override fun getTransferableDirections(backwards: BlockFace): Set<BlockFace> = ADJACENT_BLOCK_FACES.minus(backwards)
	}

	data class LightningRodNode(var axis: Axis) : FluidNode, ComplexNode {
		override val pathfindingResistance: Double = 0.5
		override fun canTransferFrom(other: Node, offset: BlockFace): Boolean = offset.axis == this.axis
		override fun canTransferTo(other: Node, offset: BlockFace): Boolean = offset.axis == this.axis
		override fun getTransferableDirections(backwards: BlockFace): Set<BlockFace> = setOf(backwards.oppositeFace)
		override fun onTranslate(movement: StarshipMovement) {
			this.axis = movement.displaceFace(this.axis.faces.first).axis
		}
	}

	data object FluidExtractorNode : FluidNode {
		override val pathfindingResistance: Double = 0.5
		override fun canTransferFrom(other: Node, offset: BlockFace): Boolean = false
		override fun canTransferTo(other: Node, offset: BlockFace): Boolean = other !is FluidInputNode
		override fun getTransferableDirections(backwards: BlockFace): Set<BlockFace> = ADJACENT_BLOCK_FACES.minus(backwards)
	}

	data object FluidInputNode : FluidNode {
		override val pathfindingResistance: Double = 0.0
		override fun canTransferFrom(other: Node, offset: BlockFace): Boolean = true
		override fun canTransferTo(other: Node, offset: BlockFace): Boolean = false
		override fun getTransferableDirections(backwards: BlockFace): Set<BlockFace> = ADJACENT_BLOCK_FACES.minus(backwards)
	}

	data object FluidMergeNode : FluidNode {
		override val pathfindingResistance: Double = 0.5
		override fun canTransferFrom(other: Node, offset: BlockFace): Boolean = other is FluidJunctionNode
		override fun canTransferTo(other: Node, offset: BlockFace): Boolean = other !is FluidJunctionNode
		override fun getTransferableDirections(backwards: BlockFace): Set<BlockFace> = ADJACENT_BLOCK_FACES.minus(backwards)
	}

	data object FluidInvertedMergeNode : FluidNode {
		override val pathfindingResistance: Double = 0.5
		override fun canTransferFrom(other: Node, offset: BlockFace): Boolean = other is LightningRodNode
		override fun canTransferTo(other: Node, offset: BlockFace): Boolean = other !is LightningRodNode
		override fun getTransferableDirections(backwards: BlockFace): Set<BlockFace> = ADJACENT_BLOCK_FACES.minus(backwards)
	}
}