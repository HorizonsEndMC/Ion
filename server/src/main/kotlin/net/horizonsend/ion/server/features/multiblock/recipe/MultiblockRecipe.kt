package net.horizonsend.ion.server.features.multiblock.recipe

import net.horizonsend.ion.server.features.multiblock.Multiblock
import org.bukkit.block.Sign
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

interface MultiblockRecipe<out T: Multiblock> {
	val multiblock: T
	val result: ItemStack

	fun matches(sign: Sign, inventory: Inventory): Boolean

	fun execute(sign: Sign, inventory: Inventory) // TODO redesign this on multiblock rewrite branch
}
