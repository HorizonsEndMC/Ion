package net.horizonsend.ion.server.features.multiblock.recipe.ingredient

import net.horizonsend.ion.server.features.multiblock.Multiblock
import org.bukkit.block.Sign

interface Consumable {
	fun consume(multiblock: Multiblock, sign: Sign)
}
