package net.horizonsend.ion.server.features.custom.items.blasters

import net.horizonsend.ion.server.configuration.PVPBalancingConfiguration.EnergyWeapons.AmmoStorage
import net.horizonsend.ion.server.features.custom.CustomItem
import net.horizonsend.ion.server.features.custom.items.components.Ammunition
import net.horizonsend.ion.server.features.custom.items.components.CustomComponentTypes
import net.horizonsend.ion.server.features.custom.items.components.CustomItemComponentManager
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory
import net.kyori.adventure.text.Component
import org.bukkit.inventory.ItemStack
import java.util.function.Supplier

class Magazine(identifier: String, displayName: Component, itemFactory: ItemFactory, private val balancingSupplier: Supplier<AmmoStorage>) : CustomItem(
	identifier,
	displayName,
	itemFactory,
) {
	val balancing get() = balancingSupplier.get()

	override val customComponents: CustomItemComponentManager = CustomItemComponentManager().apply {
		addComponent(CustomComponentTypes.AMMUNITION, Ammunition(balancingSupplier))
	}

	override fun decorateItemStack(base: ItemStack) {
		customComponents.getComponent(CustomComponentTypes.AMMUNITION).setAmmo(base, this, balancing.capacity)
	}
}
