package net.horizonsend.ion.server.features.transport.cache

import net.horizonsend.ion.server.features.transport.node.manager.holders.NetworkHolder
import net.horizonsend.ion.server.miscellaneous.utils.ADJACENT_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.axis
import org.bukkit.Axis
import org.bukkit.Material
import org.bukkit.Material.CRAFTING_TABLE
import org.bukkit.Material.END_ROD
import org.bukkit.Material.IRON_BLOCK
import org.bukkit.Material.LAPIS_BLOCK
import org.bukkit.Material.REDSTONE_BLOCK
import org.bukkit.Material.SPONGE
import org.bukkit.Material.WET_SPONGE
import org.bukkit.block.BlockFace

class PowerTransportCache(holder: NetworkHolder<*>) : TransportCache(holder) {
	private val powerNodeTypes = setOf(
		PowerNode.SpongeNode::class.java,
		PowerNode.EndRodNode::class.java,

	)

	override fun getNodeType(material: Material): CachedNode? {
		return powerNodeTypes.firstOrNull { it.isMatchingMaterial(material) }
	}

	sealed interface PowerNode : CachedNode {
		fun anyDirection(inputDirection: BlockFace) = ADJACENT_BLOCK_FACES.minus(inputDirection).map { it to 1 }

		data object SpongeNode : PowerNode {
			override fun canTransferFrom(other: CachedNode, offset: BlockFace): Boolean = true
			override fun canTransferTo(other: CachedNode, offset: BlockFace): Boolean = true
			override fun isMatchingMaterial(material: Material): Boolean = material == SPONGE || material == WET_SPONGE
			override fun getNextNodes(inputDirection: BlockFace): Collection<Pair<BlockFace, Int>> = anyDirection(inputDirection)
		}

		data class EndRodNode(val axis: Axis) : PowerNode {
			override fun canTransferFrom(other: CachedNode, offset: BlockFace): Boolean = offset.axis == this.axis
			override fun canTransferTo(other: CachedNode, offset: BlockFace): Boolean = false
			override fun isMatchingMaterial(material: Material): Boolean = material == END_ROD
			override fun getNextNodes(inputDirection: BlockFace): Collection<Pair<BlockFace, Int>> = listOf(inputDirection to 1) // Forward only
		}

		data object PowerExtractorNode : PowerNode {
			override fun canTransferFrom(other: CachedNode, offset: BlockFace): Boolean = false
			override fun canTransferTo(other: CachedNode, offset: BlockFace): Boolean = true
			override fun isMatchingMaterial(material: Material): Boolean = material == CRAFTING_TABLE
			override fun getNextNodes(inputDirection: BlockFace): Collection<Pair<BlockFace, Int>> = anyDirection(inputDirection)
		}

		data object PowerMergeNode : PowerNode {
			override fun canTransferFrom(other: CachedNode, offset: BlockFace): Boolean = other is SpongeNode
			override fun canTransferTo(other: CachedNode, offset: BlockFace): Boolean = other !is SpongeNode
			override fun isMatchingMaterial(material: Material): Boolean = material == IRON_BLOCK || material == REDSTONE_BLOCK
			override fun getNextNodes(inputDirection: BlockFace): Collection<Pair<BlockFace, Int>> = TODO()
		}

		data object PowerInvertedMergeNode : PowerNode {
			override fun canTransferFrom(other: CachedNode, offset: BlockFace): Boolean = other is EndRodNode
			override fun canTransferTo(other: CachedNode, offset: BlockFace): Boolean = other !is EndRodNode
			override fun isMatchingMaterial(material: Material): Boolean = material == LAPIS_BLOCK
			override fun getNextNodes(inputDirection: BlockFace): Collection<Pair<BlockFace, Int>> = TODO()
		}
	}
}
