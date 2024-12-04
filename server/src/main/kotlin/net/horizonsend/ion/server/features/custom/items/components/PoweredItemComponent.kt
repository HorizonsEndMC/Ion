package net.horizonsend.ion.server.features.custom.items.components

import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.custom.NewCustomItem
import net.horizonsend.ion.server.features.custom.items.CustomItems.customItem
import net.horizonsend.ion.server.features.custom.items.attribute.BonusPowerAttribute
import net.horizonsend.ion.server.features.custom.items.attribute.CustomItemAttribute
import net.horizonsend.ion.server.features.custom.items.powered.PoweredItem.PowerLoreManager.powerPrefix
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class PoweredItemComponent(private val baseMaxPower: Int) : CustomItemComponent, LoreManager {
	override fun decorateBase(baseItem: ItemStack) {
		baseItem.updateMeta {
			it.persistentDataContainer.set(NamespacedKeys.POWER, PersistentDataType.INTEGER, baseMaxPower)
		}
	}

	fun getMaxPower(itemStack: ItemStack): Int {
		val newCustomItem = itemStack /* TODO */  .customItem as NewCustomItem
		return baseMaxPower + newCustomItem.getAttributes(itemStack).filterIsInstance<BonusPowerAttribute>().sumOf { it.amount }
	}

	fun setPower(itemStack: ItemStack, amount: Int) {

	}

	fun getPower(itemStack: ItemStack): Int {

	}

	override val priority: Int = 100
	override fun shouldIncludeSeparator(): Boolean = true

	override fun getLines(itemStack: ItemStack): List<Component> {
		val power = getPower(itemStack)

		return listOf(ofChildren(
			powerPrefix,
			text(power, HEColorScheme.HE_LIGHT_GRAY),
			text(" / ", HEColorScheme.HE_MEDIUM_GRAY),
			text(getMaxPower(itemStack), HEColorScheme.HE_LIGHT_GRAY)
		).decoration(TextDecoration.ITALIC, false))
	}

	override fun getAttributes(baseItem: ItemStack): Iterable<CustomItemAttribute> = mutableListOf()
}
