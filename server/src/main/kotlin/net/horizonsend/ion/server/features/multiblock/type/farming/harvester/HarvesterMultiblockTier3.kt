package net.horizonsend.ion.server.features.multiblock.type.farming.harvester

import org.bukkit.Material

object HarvesterMultiblockTier3 : HarvesterMultiblock(Material.EMERALD_BLOCK, 3) {
	override val regionDepth: Int = 18
	override val maxPower: Int = 200_000
}
