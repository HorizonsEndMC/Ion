package net.horizonsend.ion.server.features.custom.items.component

import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.attribute.AdditionalPowerConsumption
import net.horizonsend.ion.server.features.custom.items.attribute.AdditionalPowerStorage
import net.horizonsend.ion.server.features.custom.items.attribute.CustomItemAttribute
import net.horizonsend.ion.server.features.custom.items.util.StoredValues
import net.horizonsend.ion.server.features.custom.items.util.serialization.SerializationManager
import net.horizonsend.ion.server.features.custom.items.util.serialization.token.IntegerToken
import net.horizonsend.ion.server.features.custom.items.util.updateDurability
import net.horizonsend.ion.server.miscellaneous.utils.text.itemLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.inventory.ItemStack
import kotlin.math.roundToInt

class PowerStorage(private val basePowerCapacity: Int, private val basePowerUsage: Int, val displayDurability: Boolean) : CustomItemComponent, LoreManager {
	override fun decorateBase(baseItem: ItemStack, customItem: CustomItem) {
		setPower(customItem, baseItem, basePowerCapacity)
	}

	fun getMaxPower(customItem: CustomItem, itemStack: ItemStack): Int {
		return basePowerCapacity + customItem.getAttributes(itemStack).filterIsInstance<AdditionalPowerStorage>().sumOf { it.amount }
	}

	fun setPower(customItem: CustomItem, itemStack: ItemStack, amount: Int) {
		val capacity = getMaxPower(customItem, itemStack)
		val corrected = amount.coerceAtMost(capacity)

		StoredValues.POWER.setAmount(itemStack, corrected)

		if (displayDurability) {
			updateDurability(itemStack, corrected, capacity)
		}

		customItem.refreshLore(itemStack)
	}

	fun getPower(itemStack: ItemStack): Int {
		return StoredValues.POWER.getAmount(itemStack)
	}

	fun removePower(itemStack: ItemStack, customItem: CustomItem, amount: Int) {
		val power = getPower(itemStack)
		setPower(customItem, itemStack, power - amount)
	}

	fun addPower(itemStack: ItemStack, customItem: CustomItem, amount: Int) {
		val power = getPower(itemStack)
		setPower(customItem, itemStack, power + amount)
	}

	override val priority: Int = 100
	override fun shouldIncludeSeparator(): Boolean = true

	override fun getLines(customItem: CustomItem, itemStack: ItemStack): List<Component> {
		val power = getPower(itemStack)

		return listOf(ofChildren(
			powerPrefix,
			text(power, HEColorScheme.HE_LIGHT_GRAY),
			text(" / ", HEColorScheme.HE_MEDIUM_GRAY),
			text(getMaxPower(customItem, itemStack), HEColorScheme.HE_LIGHT_GRAY)
		).itemLore)
	}

	override fun getAttributes(baseItem: ItemStack): Iterable<CustomItemAttribute> = mutableListOf()

	fun getPowerUse(itemStack: ItemStack, customItem: CustomItem): Int {
		var usage = basePowerUsage.toDouble()
		val attributes = customItem.getAttributes(itemStack).filterIsInstance<AdditionalPowerConsumption>()

		if (customItem.hasComponent(CustomComponentTypes.MOD_MANAGER)) {
			for (increase in attributes) {
				usage *= increase.multiplier
			}
		}

		return usage.roundToInt()
	}

	companion object {
		val powerPrefix = text("Power: ", HEColorScheme.HE_MEDIUM_GRAY)
	}

	override fun registerSerializers(serializationManager: SerializationManager) {
		serializationManager.addSerializedData(
			"power",
			IntegerToken,
			{ customItem, itemStack -> customItem.getComponent(CustomComponentTypes.POWER_STORAGE).getPower(itemStack) },
			{ customItem: CustomItem, itemStack: ItemStack, data: Int -> customItem.getComponent(CustomComponentTypes.POWER_STORAGE).setPower(customItem, itemStack, data) }
		)
	}
}
