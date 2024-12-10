package net.horizonsend.ion.server.features.custom.items.components

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.configuration.PVPBalancingConfiguration.EnergyWeapons.AmmoStorageBalancing
import net.horizonsend.ion.server.features.custom.NewCustomItem
import net.horizonsend.ion.server.features.custom.items.attribute.AmmunitionRefillType
import net.horizonsend.ion.server.features.custom.items.attribute.CustomItemAttribute
import net.horizonsend.ion.server.features.custom.items.objects.StoredValues.AMMO
import net.horizonsend.ion.server.features.custom.items.powered.CratePlacer.updateDurability
import net.horizonsend.ion.server.miscellaneous.utils.text.itemLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.Component.translatable
import net.kyori.adventure.text.format.NamedTextColor.AQUA
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import org.bukkit.Material
import org.bukkit.Material.matchMaterial
import org.bukkit.inventory.ItemStack
import java.util.function.Supplier

class AmmunitionComponent(val balancingSupplier: Supplier<out AmmoStorageBalancing>) : CustomItemComponent, LoreManager {

	override fun decorateBase(baseItem: ItemStack) {
		AMMO.setAmount(baseItem, balancingSupplier.get().capacity)
	}

	fun setAmmo(itemStack: ItemStack, customItem: NewCustomItem, amount: Int) {
		val corrected = amount.coerceAtMost(balancingSupplier.get().capacity)

		AMMO.setAmount(itemStack, corrected)
		customItem.refreshLore(itemStack)

		if (balancingSupplier.get().displayDurability) updateDurability(itemStack, corrected, balancingSupplier.get().capacity)
	}

	fun getAmmo(itemStack: ItemStack): Int {
		return AMMO.getAmount(itemStack)
	}

	override val priority: Int = 200
	override fun shouldIncludeSeparator(): Boolean = false

	override fun getLines(customItem: NewCustomItem, itemStack: ItemStack): List<Component> {
		val balancing = balancingSupplier.get()
		val lines = listOf(
			AMMO.formatLore(getAmmo(itemStack), balancingSupplier.get().capacity).itemLore,
			ofChildren(text("Refill: ", GRAY), translatable(matchMaterial(balancing.refillType)!!.translationKey(), AQUA)).itemLore
		)

		return lines
	}

	override fun getAttributes(baseItem: ItemStack): Iterable<CustomItemAttribute> {
		val balancing = balancingSupplier.get()
		return listOf(AmmunitionRefillType(Material.valueOf(balancing.refillType))) //TODO enum usage of material

	}
}
