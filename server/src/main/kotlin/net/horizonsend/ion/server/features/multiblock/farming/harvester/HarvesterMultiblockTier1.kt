package net.horizonsend.ion.server.features.multiblock.farming.harvester

import org.bukkit.Material

object HarvesterMultiblockTier1 : HarvesterMultiblock("&8Tier 1", Material.IRON_BLOCK) {
	override val regionDepth: Int = 9
	override val maxPower: Int = 50_000
}
