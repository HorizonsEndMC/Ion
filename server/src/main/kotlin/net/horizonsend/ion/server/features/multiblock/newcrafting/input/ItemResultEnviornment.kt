package net.horizonsend.ion.server.features.multiblock.newcrafting.input

import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.newcrafting.util.SlotModificationWrapper
import org.bukkit.inventory.ItemStack

interface ItemResultEnviornment : RecipeEnviornment {
	val multiblock: MultiblockEntity
	fun getResultItem(): ItemStack?
	fun getResultItemSlotModifier(): SlotModificationWrapper
}
