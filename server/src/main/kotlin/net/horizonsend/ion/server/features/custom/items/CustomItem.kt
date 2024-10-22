package net.horizonsend.ion.server.features.custom.items

import org.bukkit.block.Dispenser
import org.bukkit.entity.LivingEntity
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

abstract class CustomItem(val identifier: String) {
	/** Left Click **/
	open fun handlePrimaryInteract(livingEntity: LivingEntity, itemStack: ItemStack, event: PlayerInteractEvent) {}

	/** Right Click **/
	open fun handleSecondaryInteract(livingEntity: LivingEntity, itemStack: ItemStack, event: PlayerInteractEvent?) {}

	/** Swap Hands **/
	open fun handleSwapHands(livingEntity: LivingEntity, itemStack: ItemStack) {}

	/** Dispensed from a dispense **/
	open fun handleDispense(dispenser: Dispenser, slot: Int) {}

	abstract fun constructItemStack(): ItemStack
}
