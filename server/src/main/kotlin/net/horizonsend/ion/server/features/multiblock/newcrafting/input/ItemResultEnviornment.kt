package net.horizonsend.ion.server.features.multiblock.newcrafting.input

import net.horizonsend.ion.server.features.multiblock.newcrafting.util.SlotModificationWrapper
import org.bukkit.inventory.ItemStack

interface ItemResultEnviornment : RecipeEnviornment {
	fun getResultItem(): ItemStack?
	fun getResultItemSlotModifier(): SlotModificationWrapper
}
