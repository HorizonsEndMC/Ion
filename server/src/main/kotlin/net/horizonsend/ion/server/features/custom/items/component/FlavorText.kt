package net.horizonsend.ion.server.features.custom.items.component

import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.attribute.CustomItemAttribute
import net.kyori.adventure.text.Component
import org.bukkit.inventory.ItemStack

class FlavorText(val lore: List<Component>) : CustomItemComponent, LoreManager {
    override fun decorateBase(baseItem: ItemStack, customItem: CustomItem) {}

    override fun getAttributes(baseItem: ItemStack): Iterable<CustomItemAttribute> = listOf()

    override val priority = 5

    override fun getLines(customItem: CustomItem, itemStack: ItemStack): List<Component> = lore

    override fun shouldIncludeSeparator(): Boolean = false
}