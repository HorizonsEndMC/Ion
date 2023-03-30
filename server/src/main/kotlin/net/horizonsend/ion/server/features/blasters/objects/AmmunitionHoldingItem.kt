package net.horizonsend.ion.server.features.blasters.objects

import net.horizonsend.ion.server.features.customitems.CustomItem
import net.horizonsend.ion.server.miscellaneous.NamespacedKeys.AMMO
import net.horizonsend.ion.server.miscellaneous.NamespacedKeys.CUSTOM_ITEM
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.Component.translatable
import net.kyori.adventure.text.format.NamedTextColor.AQUA
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.TextDecoration.ITALIC
import net.starlegacy.util.updateMeta
import org.bukkit.Material
import org.bukkit.Material.matchMaterial
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType.INTEGER
import org.bukkit.persistence.PersistentDataType.STRING

abstract class AmmunitionHoldingItem(
	identifier: String,

	private val material: Material,
	private val customModelData: Int,
	private val displayName: Component,

	private val shouldDeleteItem: Boolean = false
) : CustomItem(identifier) {

	override fun constructItemStack(): ItemStack {
		val ammoCount = empty()
			.decoration(ITALIC, false)
			.append(text("Ammo: ", GRAY))
			.append(text(getMaximumAmmunition(), AQUA))
			.append(text(" / ", GRAY))
			.append(text(getMaximumAmmunition(), AQUA))
		val refillType = empty()
			.decoration(ITALIC, false)
			.append(text("Refill: ", GRAY))
			.append(translatable(matchMaterial(getTypeRefill())!!.translationKey(), AQUA))
		val magazineType = if (this is Blaster<*>) {
			empty()
				.decoration(ITALIC, false)
				.append(text("Magazine: ", GRAY))
				.append(text(getTypeMagazine(), AQUA))
		} else null

		return ItemStack(material).updateMeta {
			it.setCustomModelData(customModelData)
			it.displayName(displayName)
			it.persistentDataContainer.set(CUSTOM_ITEM, STRING, identifier)
			it.persistentDataContainer.set(AMMO, INTEGER, getMaximumAmmunition())
			it.lore(listOf(ammoCount, refillType, magazineType))
			it.isUnbreakable = true
		}
	}

	abstract fun getMaximumAmmunition(): Int
	abstract fun getTypeMagazine(): String
	abstract fun getTypeRefill(): String
	abstract fun getAmmoPerRefill(): Int

	fun getAmmunition(itemStack: ItemStack): Int {
		// stupid undefined nullability
		return itemStack.itemMeta?.persistentDataContainer?.get(AMMO, INTEGER)?.coerceIn(0, getMaximumAmmunition()) ?: 0
	}

	open fun setAmmunition(itemStack: ItemStack, inventory: Inventory, ammunition: Int) {
		@Suppress("NAME_SHADOWING") val ammunition = ammunition.coerceIn(0, getMaximumAmmunition())

		val ammoCount = empty()
			.decoration(ITALIC, false)
			.append(text("Ammo: ", GRAY))
			.append(text(ammunition, AQUA))
			.append(text(" / ", GRAY))
			.append(text(getMaximumAmmunition(), AQUA))
		val refillType = empty()
			.decoration(ITALIC, false)
			.append(text("Refill: ", GRAY))
			.append(translatable(matchMaterial(getTypeRefill())!!.translationKey(), AQUA))
		val magazineType = if (this is Blaster<*>) {
			empty()
				.decoration(ITALIC, false)
				.append(text("Magazine: ", GRAY))
				.append(text(getTypeMagazine(), AQUA))
		} else null

		itemStack.editMeta {
			it.lore(listOf(ammoCount, refillType, magazineType))
			it.persistentDataContainer.set(AMMO, INTEGER, ammunition)
		}

		if (ammunition <= 0 && shouldDeleteItem) {
			inventory.removeItemAnySlot(itemStack)
		}

		(inventory.holder as? Player)?.updateInventory()
	}
}
