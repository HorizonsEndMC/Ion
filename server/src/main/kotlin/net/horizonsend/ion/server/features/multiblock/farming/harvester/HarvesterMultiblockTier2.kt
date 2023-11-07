package net.horizonsend.ion.server.features.multiblock.farming.harvester

import org.bukkit.Material

object HarvesterMultiblockTier2 : HarvesterMultiblock(Material.GOLD_BLOCK, 2) {
	override val regionDepth: Int = 15
	override val maxPower: Int = 100_000
}
