package net.horizonsend.ion.server.features.custom.items.component

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.wrap
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.attribute.CustomItemAttribute
import net.horizonsend.ion.server.features.multiblock.PrePackaged
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock.Companion.getDescription
import net.horizonsend.ion.server.miscellaneous.utils.text.itemLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import org.bukkit.inventory.ItemStack
import java.util.Locale

object StoredMultiblock : CustomItemComponent, LoreManager {
	override val priority: Int = 1

	override fun decorateBase(baseItem: ItemStack, customItem: CustomItem) {}

	override fun getAttributes(baseItem: ItemStack): Iterable<CustomItemAttribute> = listOf()

	override fun getLines(customItem: CustomItem, itemStack: ItemStack): List<Component> {
		val multiblock = PrePackaged.getTokenData(itemStack) ?: return listOf()
		val description = multiblock.getDescription().wrap(150).map { it.itemLore }

		return description + listOf(
			Component.empty(),
			ofChildren(text("Multiblock: ", GRAY), text(multiblock.name.replaceFirstChar { char -> char.uppercase(Locale.getDefault()) })).itemLore,
			ofChildren(text("Variant: ", GRAY), text(multiblock.javaClass.simpleName)).itemLore,
			text("Left click to preview").itemLore,
			text("Right click to place").itemLore
		)
	}

	override fun shouldIncludeSeparator(): Boolean {
		return false
	}
}
