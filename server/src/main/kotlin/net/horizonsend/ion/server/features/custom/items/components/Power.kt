package net.horizonsend.ion.server.features.custom.items.components

import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.custom.NewCustomItem
import net.horizonsend.ion.server.features.custom.items.attribute.AdditionalPowerStorage
import net.horizonsend.ion.server.features.custom.items.attribute.CustomItemAttribute
import net.horizonsend.ion.server.features.custom.items.mods.tool.PowerUsageIncrease
import net.horizonsend.ion.server.features.custom.items.objects.StoredValues
import net.horizonsend.ion.server.features.custom.items.util.updateDurability
import net.horizonsend.ion.server.miscellaneous.utils.text.itemLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.entity.Damageable
import org.bukkit.inventory.ItemStack
import kotlin.math.roundToInt

class Power(private val basePowerCapacity: Int, private val basePowerUsage: Int, val displayDurability: Boolean) : CustomItemComponent, LoreManager {
	override fun decorateBase(baseItem: ItemStack, customItem: NewCustomItem) {
		setPower(customItem, baseItem, basePowerCapacity)
	}

	fun getMaxPower(customItem: NewCustomItem, itemStack: ItemStack): Int {
		return basePowerCapacity + customItem.getAttributes(itemStack).filterIsInstance<AdditionalPowerStorage>().sumOf { it.amount }
	}

	fun setPower(customItem: NewCustomItem, itemStack: ItemStack, amount: Int) {
		val capacity = getMaxPower(customItem, itemStack)
		val corrected = amount.coerceAtMost(capacity)

		StoredValues.POWER.setAmount(itemStack, corrected)

		if (displayDurability && itemStack.itemMeta is Damageable) {
			updateDurability(itemStack, corrected, capacity)
		}

		customItem.refreshLore(itemStack)
	}

	fun getPower(itemStack: ItemStack): Int {
		return StoredValues.POWER.getAmount(itemStack)
	}

	fun removePower(itemStack: ItemStack, customItem: NewCustomItem, amount: Int) {
		val power = getPower(itemStack)
		setPower(customItem, itemStack, power - amount)
	}

	fun addPower(itemStack: ItemStack, customItem: NewCustomItem, amount: Int) {
		val power = getPower(itemStack)
		setPower(customItem, itemStack, power + amount)
	}

	override val priority: Int = 100
	override fun shouldIncludeSeparator(): Boolean = true

	override fun getLines(customItem: NewCustomItem, itemStack: ItemStack): List<Component> {
		val power = getPower(itemStack)

		return listOf(ofChildren(
			powerPrefix,
			text(power, HEColorScheme.HE_LIGHT_GRAY),
			text(" / ", HEColorScheme.HE_MEDIUM_GRAY),
			text(getMaxPower(customItem, itemStack), HEColorScheme.HE_LIGHT_GRAY)
		).itemLore)
	}

	override fun getAttributes(baseItem: ItemStack): Iterable<CustomItemAttribute> = mutableListOf()

	fun getPowerUse(itemStack: ItemStack, customItem: NewCustomItem): Int {
		var usage = basePowerUsage.toDouble()
		val attributes = customItem.getAttributes(itemStack).filterIsInstance<PowerUsageIncrease>()

		if (customItem.hasComponent(CustomComponentTypes.MODDED_ITEM)) {
			for (increase in attributes) {
				usage *= increase.usageMultiplier
			}
		}

		return usage.roundToInt()
	}

	companion object {
		val powerPrefix = text("Power: ", HEColorScheme.HE_MEDIUM_GRAY)
	}
}
