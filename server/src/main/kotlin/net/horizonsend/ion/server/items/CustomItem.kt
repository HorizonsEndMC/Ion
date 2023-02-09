package net.horizonsend.ion.server.items

import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack

abstract class CustomItem(val identifier: String) {
	open fun handlePrimaryInteract(livingEntity: LivingEntity, itemStack: ItemStack) {}
	open fun handleSecondaryInteract(livingEntity: LivingEntity, itemStack: ItemStack) {}
	open fun handleTertiaryInteract(livingEntity: LivingEntity, itemStack: ItemStack) {}
	abstract fun constructItemStack(): ItemStack
}
