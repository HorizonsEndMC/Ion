package net.horizonsend.ion.server.features.custom.items.objects

import net.horizonsend.ion.server.features.custom.items.blasters.Blaster
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys.AMMO
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys.CUSTOM_ITEM
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.Component.translatable
import net.kyori.adventure.text.format.NamedTextColor.AQUA
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.TextDecoration.ITALIC
import org.bukkit.Material.matchMaterial
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.persistence.PersistentDataType.INTEGER
import org.bukkit.persistence.PersistentDataType.STRING
import kotlin.math.roundToInt

interface AmmunitionHoldingItem : CustomModeledItem {
	val identifier: String
	val displayName: Component

	// These are methods so that they can provide updated values on balancing refreshes
	fun getMaximumAmmunition(): Int
	fun getTypeRefill(): String
	fun getAmmoPerRefill(): Int
	fun getConsumesAmmo(): Boolean

	fun getRefillTypeComponent() = if (getConsumesAmmo()) {
		empty()
			.decoration(ITALIC, false)
			.append(text("Refill: ", GRAY))
			.append(translatable(matchMaterial(getTypeRefill())!!.translationKey(), AQUA))
	} else null

	fun getAmmoCountComponent(count: Int): Component = StoredValues.AMMO.formatLore(count, getMaximumAmmunition())

	fun getFullItem(): ItemStack {
		val ammoCount = getAmmoCountComponent(getMaximumAmmunition())

		val base = getModeledItem()

		return base.updateMeta {
			it.displayName(displayName)
			it.persistentDataContainer.set(CUSTOM_ITEM, STRING, identifier)
			it.persistentDataContainer.set(AMMO, INTEGER, getMaximumAmmunition())
			it.lore(listOf(ammoCount))
		}
	}

	fun getAmmunition(itemStack: ItemStack): Int = StoredValues.AMMO.getAmount(itemStack)

	open fun setAmmunition(itemStack: ItemStack, inventory: Inventory, ammunition: Int) {
		@Suppress("NAME_SHADOWING") val ammunition = ammunition.coerceIn(0, getMaximumAmmunition())

		val ammoCountComponent = StoredValues.AMMO.formatLore(ammunition, getMaximumAmmunition())

		val refillTypeComponent = if (getConsumesAmmo()) {
			empty()
				.decoration(ITALIC, false)
				.append(text("Refill: ", GRAY))
				.append(translatable(matchMaterial(getTypeRefill())!!.translationKey(), AQUA))
		} else null

		val magazineTypeComponent = if (this is Blaster<*> && getConsumesAmmo()) {
			empty()
				.decoration(ITALIC, false)
				.append(text("Magazine: ", GRAY))
				.append(magazineType.displayName).color(AQUA)
		} else null

		StoredValues.AMMO.setAmount(itemStack, ammunition)

		itemStack.editMeta {
			it.lore(listOf(ammoCountComponent, refillTypeComponent, magazineTypeComponent))
			it.isUnbreakable = false
			(it as Damageable).damage = (itemStack.type.maxDurability - ammunition.toDouble() / getMaximumAmmunition() * itemStack.type.maxDurability).roundToInt()
		}

		(inventory.holder as? Player)?.updateInventory()
	}
}
