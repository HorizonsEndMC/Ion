package net.horizonsend.ion.server.features.custom.items.powered

import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.custom.items.objects.LoreCustomItem
import net.horizonsend.ion.server.features.custom.items.objects.StoredValues
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import kotlin.math.roundToInt

interface PoweredItem : LoreCustomItem {
	/** Whether the power should be displayed in the durability bar */
	val displayDurability: Boolean

	fun getPowerCapacity(itemStack: ItemStack): Int

	fun getPower(itemStack: ItemStack): Int {
		return StoredValues.POWER.getAmount(itemStack).coerceIn(0..getPowerCapacity(itemStack))
	}

	fun getPowerUse(itemStack: ItemStack): Int

	fun setPower(itemStack: ItemStack, 	amount: Int) {
		val capacity = getPowerCapacity(itemStack)
		val corrected = amount.coerceAtMost(capacity)
		StoredValues.POWER.setAmount(itemStack, corrected)

		itemStack.updateMeta {
			if (displayDurability && it is Damageable) {
				it.damage = (itemStack.type.maxDurability - amount.toDouble() / capacity * itemStack.type.maxDurability).roundToInt()
			}
		}

		rebuildLore(itemStack)
	}

	/** Format the lore of the power description **/
	fun getPowerLore(item: ItemStack): List<Component> = listOf(ofChildren(
		powerPrefix,
		text(getPower(item), HEColorScheme.HE_LIGHT_GRAY),
		text(" / ", HEColorScheme.HE_MEDIUM_GRAY),
		text(getPowerCapacity(item), HEColorScheme.HE_LIGHT_GRAY)
	).decoration(TextDecoration.ITALIC, false))

	fun removePower(itemStack: ItemStack, amount: Int) {
		val power = getPower(itemStack)
		setPower(itemStack, power - amount)
	}

	companion object {
		private val powerPrefix = text("Power: ", HEColorScheme.HE_MEDIUM_GRAY)
	}
}
