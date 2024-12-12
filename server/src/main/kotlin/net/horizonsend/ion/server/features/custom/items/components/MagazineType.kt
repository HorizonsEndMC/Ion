package net.horizonsend.ion.server.features.custom.items.components

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.configuration.PVPBalancingConfiguration.EnergyWeapons.Balancing
import net.horizonsend.ion.server.features.custom.NewCustomItem
import net.horizonsend.ion.server.features.custom.items.attribute.CustomItemAttribute
import net.horizonsend.ion.server.miscellaneous.utils.text.itemLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import org.bukkit.inventory.ItemStack
import java.util.function.Supplier

class MagazineType<T : Balancing>(val balancing: Supplier<T>, private val magazineType: Supplier<NewCustomItem>) : CustomItemComponent, LoreManager {

	override fun decorateBase(baseItem: ItemStack, customItem: NewCustomItem) {}

	override fun getAttributes(baseItem: ItemStack): Iterable<CustomItemAttribute> {
		return listOf()
	}

	override fun getLines(customItem: NewCustomItem, itemStack: ItemStack): List<Component> {
		return listOf(ofChildren(text("Magazine: ", GRAY), magazineType.get().displayName).itemLore)
	}

	override fun shouldIncludeSeparator(): Boolean = false
	override val priority: Int = 199
}
