package net.starlegacy.feature.machine

import net.starlegacy.feature.misc.CustomItem
import net.starlegacy.feature.misc.CustomItems
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

private fun customItem(customItem: CustomItem): ItemStack = customItem.singleItem()
private fun itemStack(material: Material): ItemStack = ItemStack(material, 1)

enum class GeneratorFuel(private val item: ItemStack, val cooldown: Int, val power: Int) {
    HYDROGEN(customItem(CustomItems.GAS_CANISTER_HYDROGEN), cooldown = 200, power = 300),
    OXYGEN(customItem(CustomItems.GAS_CANISTER_OXYGEN), cooldown = 150, power = 100),
    NITROGEN(customItem(CustomItems.GAS_CANISTER_NITROGEN), cooldown = 100, power = 250),
    URANIUM(customItem(CustomItems.MINERAL_URANIUM), cooldown = 2000, power = 5000),
    COAL(itemStack(Material.COAL), cooldown = 40, power = 500),
    CHARCOAL(itemStack(Material.CHARCOAL), cooldown = 40, power = 400),
    COAL_BLOCK(itemStack(Material.COAL_BLOCK), cooldown = 300, power = 4000),
    REDSTONE(itemStack(Material.REDSTONE), cooldown = 75, power = 750),
    REDSTONE_BLOCK(itemStack(Material.REDSTONE_BLOCK), cooldown = 350, power = 6500);

    companion object {
        private val itemMap: Map<String, GeneratorFuel> = values().associateBy { createKey(it.item) }

        @JvmStatic
        fun getFuel(item: ItemStack): GeneratorFuel? = itemMap[createKey(item)]

        private fun createKey(it: ItemStack) = CustomItems[it]?.id ?: it.type.name
    }

    fun getItem(): ItemStack = item.clone()
}
