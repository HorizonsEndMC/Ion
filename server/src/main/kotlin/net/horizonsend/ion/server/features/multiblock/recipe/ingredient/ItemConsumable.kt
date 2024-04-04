package net.horizonsend.ion.server.features.multiblock.recipe.ingredient

import net.horizonsend.ion.server.features.multiblock.Multiblock
import org.bukkit.block.Sign
import org.bukkit.inventory.ItemStack

interface ItemConsumable {
	fun consume(multiblock: Multiblock, sign: Sign, itemStack: ItemStack)
}
