package net.horizonsend.ion.server.features.custom.items.type.weapon.blaster

import net.horizonsend.ion.server.configuration.PVPBalancingConfiguration.EnergyWeapons.AmmoStorage
import net.horizonsend.ion.server.core.registries.IonRegistryKey
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.component.AmmunitionStorage
import net.horizonsend.ion.server.features.custom.items.component.CustomComponentTypes
import net.horizonsend.ion.server.features.custom.items.component.CustomItemComponentManager
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory
import net.kyori.adventure.text.Component
import org.bukkit.inventory.ItemStack
import java.util.function.Supplier

class Magazine(key: IonRegistryKey<CustomItem>, displayName: Component, itemFactory: ItemFactory, private val balancingSupplier: Supplier<AmmoStorage>) : CustomItem(
	key,
	displayName,
	itemFactory,
) {
	val balancing get() = balancingSupplier.get()

	override val customComponents: CustomItemComponentManager = CustomItemComponentManager(serializationManager).apply {
		addComponent(CustomComponentTypes.AMMUNITION_STORAGE, AmmunitionStorage(balancingSupplier))
	}

	override fun decorateItemStack(base: ItemStack) {
		customComponents.getComponent(CustomComponentTypes.AMMUNITION_STORAGE).setAmmo(base, this, balancing.capacity)
	}
}
