package net.horizonsend.ion.server.customitems

import kotlin.math.min
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack

abstract class CustomItem {
	abstract val customModelData: Int

	open val stackLimit: Int = 1

	open fun onPrimaryInteract(source: LivingEntity, item: ItemStack) {}
	open fun onSecondaryInteract(entity: LivingEntity, item: ItemStack) {}

	fun combine(items: Pair<ItemStack, ItemStack>): Pair<ItemStack, ItemStack> {
		val total = items.first.amount + items.second.amount

		items.first.amount = min(total, stackLimit)
		items.second.amount = total - items.first.amount

		return items
	}
}