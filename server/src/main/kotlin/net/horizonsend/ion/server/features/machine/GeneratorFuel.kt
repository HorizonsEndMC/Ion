package net.horizonsend.ion.server.features.machine

import net.horizonsend.ion.server.features.custom.CustomItemRegistry
import net.horizonsend.ion.server.features.custom.NewCustomItem
import net.horizonsend.ion.server.features.custom.items.CustomItems.customItem
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

private fun customItem(customItem: NewCustomItem): ItemStack = customItem.constructItemStack()
private fun itemStack(material: Material): ItemStack = ItemStack(material, 1)

enum class GeneratorFuel(private val item: ItemStack, val cooldown: Int, val power: Int) {
	URANIUM(customItem(CustomItemRegistry.URANIUM), cooldown = 2000, power = 9000),
	COAL(itemStack(Material.COAL), cooldown = 40, power = 500),
	CHARCOAL(itemStack(Material.CHARCOAL), cooldown = 40, power = 400),
	COAL_BLOCK(itemStack(Material.COAL_BLOCK), cooldown = 300, power = 4000),
	REDSTONE(itemStack(Material.REDSTONE), cooldown = 75, power = 750),
	REDSTONE_BLOCK(itemStack(Material.REDSTONE_BLOCK), cooldown = 350, power = 6500),
	DRIED_KELP_BLOCK(itemStack(Material.DRIED_KELP_BLOCK), cooldown = 60, power = 800);

	companion object {
		private val itemMap: Map<String, GeneratorFuel> = values().associateBy { createKey(it.item) }

		@JvmStatic
		fun getFuel(item: ItemStack): GeneratorFuel? = itemMap[createKey(item)]

		private fun createKey(it: ItemStack) = it.customItem?.identifier ?: it.type.name
	}

	fun getItem(): ItemStack = item.clone()
}
