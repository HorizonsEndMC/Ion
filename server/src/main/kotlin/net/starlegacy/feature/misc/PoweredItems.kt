package net.starlegacy.feature.misc

import net.starlegacy.util.colorize
import net.starlegacy.util.stripColor
import org.bukkit.inventory.ItemStack
import kotlin.math.max
import kotlin.math.min

val ITEM_POWER_PREFIX = "&8Power: &7".colorize()

fun isPowerable(itemStack: ItemStack?): Boolean = CustomItems[itemStack] is PoweredCustomItem

/**
 * Get the power of the item
 * @return The item's power if it is powerable, otherwise -1
 */
fun getPower(itemStack: ItemStack): Int {
	if (!isPowerable(itemStack)) {
		return -1
	}
	val lore = itemStack.lore ?: return -1
	return lore[0].stripColor().split(' ').last().toInt()
}

/**
 * Get the maximum amount of power an item can hold
 * @return The item's max power if it is powerable, otherwise -1
 */
fun getMaxPower(itemStack: ItemStack): Int {
	val poweredCustomItem = CustomItems[itemStack] as? PoweredCustomItem ?: return -1
	return poweredCustomItem.maxPower
}

/**
 * Set the power of the item to the new power if it is powerable
 * Automatically limits to max power
 * @return The old power if it was a powerable item, otherwise -1
 */
fun setPower(itemStack: ItemStack, newPower: Int): Int {
	val poweredCustomItem = CustomItems[itemStack] as? PoweredCustomItem ?: return -1

	val oldPower = getPower(itemStack)

	val lore: MutableList<String> = itemStack.lore ?: mutableListOf()
	val text = "$ITEM_POWER_PREFIX${max(min(newPower, poweredCustomItem.maxPower), 0)}"
	if (lore.size == 0) {
		lore.add(text)
	} else {
		lore[0] = text
	}
	itemStack.lore = lore

	return oldPower
}

/**
 * Adds the given amount of power to the item if it is powerable
 * Automatically limits to max power
 * @return The old power if it was powerable, otherwise -1
 */
fun addPower(itemStack: ItemStack, amount: Int): Int {
	val power = getPower(itemStack)
	if (power == -1) return -1

	return setPower(itemStack, power + amount)
}

/**
 * removes the given amount of power to the item if it is powerable
 * Automatically limits to max power
 * @return The old power if it was powerable, otherwise -1
 */
fun removePower(itemStack: ItemStack, amount: Int): Int {
	val power = getPower(itemStack)
	if (power == -1) return -1

	return setPower(itemStack, power - amount)
}
