package net.horizonsend.ion.server.features.transport.nodes.types

import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.FluidStoringEntity
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.horizonsend.ion.server.features.transport.nodes.types.Node.Companion.adjacentMinusBackwards
import net.horizonsend.ion.server.features.transport.util.CacheType
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.miscellaneous.utils.axis
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.faces
import org.bukkit.Axis
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace

sealed interface FluidNode : Node {
	override val cacheType: CacheType get() = CacheType.FLUID

	data class FluidJunctionNode(val channel: Material) : FluidNode {
        override fun canTransferFrom(other: Node, offset: BlockFace): Boolean = if (other is FluidJunctionNode) other.channel == channel else true
		override fun canTransferTo(other: Node, offset: BlockFace): Boolean = if (other is FluidJunctionNode) other.channel == channel else true
		override fun getTransferableDirections(backwards: BlockFace): Set<BlockFace> = adjacentMinusBackwards(backwards)
	}

	data class LightningRodNode(var axis: Axis) : FluidNode, ComplexNode {
        override fun canTransferFrom(other: Node, offset: BlockFace): Boolean = offset.axis == this.axis
		override fun canTransferTo(other: Node, offset: BlockFace): Boolean = offset.axis == this.axis
		override fun getTransferableDirections(backwards: BlockFace): Set<BlockFace> = setOf(backwards.oppositeFace)
		override fun displace(movement: StarshipMovement) {
			this.axis = movement.displaceFace(this.axis.faces.first).axis
		}
	}

	data object FluidExtractorNode : FluidNode {
        override fun canTransferFrom(other: Node, offset: BlockFace): Boolean = false
		override fun canTransferTo(other: Node, offset: BlockFace): Boolean = other !is FluidInputNode
		override fun getTransferableDirections(backwards: BlockFace): Set<BlockFace> = adjacentMinusBackwards(backwards)
	}

	data object FluidInputNode : FluidNode {
        override fun canTransferFrom(other: Node, offset: BlockFace): Boolean = true
		override fun canTransferTo(other: Node, offset: BlockFace): Boolean = false
		override fun getTransferableDirections(backwards: BlockFace): Set<BlockFace> = adjacentMinusBackwards(backwards)
		fun getFluidEntities(world: World, location: BlockKey) = world.ion.inputManager.getHolders(CacheType.FLUID, location).filterIsInstance<FluidStoringEntity>()
	}

	data object FluidMergeNode : FluidNode {
        override fun canTransferFrom(other: Node, offset: BlockFace): Boolean = other is FluidJunctionNode
		override fun canTransferTo(other: Node, offset: BlockFace): Boolean = other !is FluidJunctionNode
		override fun getTransferableDirections(backwards: BlockFace): Set<BlockFace> = adjacentMinusBackwards(backwards)
	}

	data object FluidInvertedMergeNode : FluidNode {
        override fun canTransferFrom(other: Node, offset: BlockFace): Boolean = other is LightningRodNode
		override fun canTransferTo(other: Node, offset: BlockFace): Boolean = other !is LightningRodNode
		override fun getTransferableDirections(backwards: BlockFace): Set<BlockFace> = adjacentMinusBackwards(backwards)
	}
}
