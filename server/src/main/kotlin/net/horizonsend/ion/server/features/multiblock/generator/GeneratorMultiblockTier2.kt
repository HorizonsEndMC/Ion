package net.horizonsend.ion.server.features.multiblock.generator

import org.bukkit.Material

object GeneratorMultiblockTier2 : GeneratorMultiblock("&eTier 2", Material.GOLD_BLOCK) {
	override val maxPower = 175_000
	override val speed = 1.25
}
