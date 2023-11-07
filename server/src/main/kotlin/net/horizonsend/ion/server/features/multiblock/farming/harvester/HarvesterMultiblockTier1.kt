package net.horizonsend.ion.server.features.multiblock.farming.harvester

import org.bukkit.Material

object HarvesterMultiblockTier1 : HarvesterMultiblock(Material.IRON_BLOCK, 1) {
	override val regionDepth: Int = 9
	override val maxPower: Int = 50_000
}
