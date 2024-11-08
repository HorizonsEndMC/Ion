package net.horizonsend.ion.server.features.transport.cache

import net.horizonsend.ion.server.features.transport.node.manager.holders.NetworkHolder
import net.horizonsend.ion.server.miscellaneous.utils.ADJACENT_BLOCK_FACES
import org.bukkit.Material
import org.bukkit.Material.END_ROD
import org.bukkit.Material.SPONGE
import org.bukkit.Material.WET_SPONGE
import org.bukkit.block.BlockFace

class PowerTransportCache(holder: NetworkHolder<*>) : TransportCache(holder) {
	private val powerNodeTypes = setOf(
		PowerNode.SpongeNode,
		PowerNode.EndRodNode
	)

	override fun getNodeType(material: Material): CachedNode? {
		return powerNodeTypes.firstOrNull { it.isMatchingMaterial(material) }
	}

	sealed interface PowerNode : CachedNode {
		data object SpongeNode : PowerNode {
			override fun isMatchingMaterial(material: Material): Boolean = material == SPONGE || material == WET_SPONGE
			override fun getNextNodes(inputDirection: BlockFace): Set<BlockFace> = ADJACENT_BLOCK_FACES.minus(inputDirection)
		}

		data object EndRodNode : PowerNode {
			override fun isMatchingMaterial(material: Material): Boolean = material == END_ROD
			override fun getNextNodes(inputDirection: BlockFace): Set<BlockFace> = setOf(inputDirection)
		}


	}
}
