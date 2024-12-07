package net.horizonsend.ion.server.features.custom.items.components

import net.horizonsend.ion.server.configuration.PVPBalancingConfiguration.EnergyWeapons.AmmoLoaderUsable
import net.horizonsend.ion.server.configuration.PVPBalancingConfiguration.EnergyWeapons.AmmoStorageBalancing
import net.horizonsend.ion.server.features.custom.NewCustomItem
import net.horizonsend.ion.server.features.custom.items.attribute.AmmunitionRefillType
import net.horizonsend.ion.server.features.custom.items.attribute.CustomItemAttribute
import net.horizonsend.ion.server.features.custom.items.objects.StoredValues.AMMO
import net.horizonsend.ion.server.features.custom.items.powered.CratePlacer.updateDurability
import net.horizonsend.ion.server.miscellaneous.utils.text.itemLore
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Damageable
import org.bukkit.inventory.ItemStack
import java.util.function.Supplier

class AmmunitionComponent(val balancingSupplier: Supplier<out AmmoStorageBalancing>) : CustomItemComponent, LoreManager {
	override fun decorateBase(baseItem: ItemStack) {
		AMMO.setAmount(baseItem, balancingSupplier.get().capacity)
	}

	fun setAmmo(itemStack: ItemStack, amount: Int) {
		val corrected = amount.coerceAtMost(balancingSupplier.get().capacity)

		AMMO.setAmount(itemStack, corrected)

		if (balancingSupplier.get().displayDurability && itemStack.itemMeta is Damageable) {
			updateDurability(itemStack, corrected, balancingSupplier.get().capacity)
		}
	}

	fun getAmmo(itemStack: ItemStack): Int {
		return AMMO.getAmount(itemStack)
	}

	override val priority: Int = 200
	override fun shouldIncludeSeparator(): Boolean = true

	override fun getLines(customItem: NewCustomItem, itemStack: ItemStack): List<Component> {
		return listOf(AMMO.formatLore(getAmmo(itemStack), balancingSupplier.get().capacity).itemLore)
	}

	override fun getAttributes(baseItem: ItemStack): Iterable<CustomItemAttribute> {
		val balancing = balancingSupplier.get()
		if (balancing is AmmoLoaderUsable) return listOf(AmmunitionRefillType(Material.valueOf(balancing.refillType))) //TODO enum usage of material
		return listOf()
	}
}
