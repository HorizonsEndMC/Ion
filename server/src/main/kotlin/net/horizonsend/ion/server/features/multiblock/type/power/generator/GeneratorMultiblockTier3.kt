package net.horizonsend.ion.server.features.multiblock.type.power.generator

import org.bukkit.Material

object GeneratorMultiblockTier3 : GeneratorMultiblock("&bTier 3", Material.DIAMOND_BLOCK) {
	override val speed = 1.5
	override val maxPower = 250_000
}
