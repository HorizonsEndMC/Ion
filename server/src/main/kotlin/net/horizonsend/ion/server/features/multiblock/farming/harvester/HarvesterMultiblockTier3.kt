package net.horizonsend.ion.server.features.multiblock.farming.harvester

import org.bukkit.Material

object HarvesterMultiblockTier3 : HarvesterMultiblock("&aTier 3", Material.EMERALD_BLOCK) {
	override val regionDepth: Int = 18
	override val maxPower: Int = 200_000
}
