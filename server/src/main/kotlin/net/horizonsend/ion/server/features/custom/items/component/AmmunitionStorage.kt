package net.horizonsend.ion.server.features.custom.items.component

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.configuration.PVPBalancingConfiguration.EnergyWeapons.AmmoStorageBalancing
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.attribute.AmmunitionRefillType
import net.horizonsend.ion.server.features.custom.items.attribute.CustomItemAttribute
import net.horizonsend.ion.server.features.custom.items.util.StoredValues.AMMO
import net.horizonsend.ion.server.features.custom.items.util.serialization.SerializationManager
import net.horizonsend.ion.server.features.custom.items.util.serialization.token.IntegerToken
import net.horizonsend.ion.server.features.custom.items.util.updateDurability
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

class AmmunitionStorage(val balancingSupplier: Supplier<out AmmoStorageBalancing>) : CustomItemComponent, LoreManager {

	override fun decorateBase(baseItem: ItemStack, customItem: CustomItem) {
		AMMO.setAmount(baseItem, balancingSupplier.get().capacity)
	}

	fun setAmmo(itemStack: ItemStack, customItem: CustomItem, amount: Int) {
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

	override fun getLines(customItem: CustomItem, itemStack: ItemStack): List<Component> {
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

	override fun registerSerializers(serializationManager: SerializationManager) {
		serializationManager.addSerializedData(
			"ammo",
			IntegerToken,
			{ customItem, itemStack -> customItem.getComponent(CustomComponentTypes.AMMUNITION_STORAGE).getAmmo(itemStack) },
			{ customItem: CustomItem, itemStack: ItemStack, data: Int -> customItem.getComponent(CustomComponentTypes.AMMUNITION_STORAGE).setAmmo(itemStack, customItem, data) }
		)
	}
}
