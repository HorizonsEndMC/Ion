package net.horizonsend.ion.server.features.multiblock.crafting.input

import net.horizonsend.ion.server.features.multiblock.crafting.util.SlotModificationWrapper
import org.bukkit.inventory.ItemStack

interface ItemResultEnviornment : RecipeEnviornment {
	fun getResultItem(): ItemStack?
	fun getResultItemSlotModifier(): SlotModificationWrapper
}
