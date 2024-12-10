package net.horizonsend.ion.server.features.custom.items.blasters

import net.horizonsend.ion.server.configuration.PVPBalancingConfiguration.EnergyWeapons.AmmoStorage
import net.horizonsend.ion.server.features.custom.NewCustomItem
import net.horizonsend.ion.server.features.custom.items.components.AmmunitionComponent
import net.horizonsend.ion.server.features.custom.items.components.CustomComponentType
import net.horizonsend.ion.server.features.custom.items.components.CustomItemComponentManager
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory
import net.kyori.adventure.text.Component
import org.bukkit.inventory.ItemStack
import java.util.function.Supplier

class Magazine(identifier: String, displayName: Component, itemFactory: ItemFactory, private val balancingSupplier: Supplier<AmmoStorage>) : NewCustomItem(
	identifier,
	displayName,
	itemFactory,
) {
	val balancing get() = balancingSupplier.get()

	override val customComponents: CustomItemComponentManager = CustomItemComponentManager().apply {
		addComponent(CustomComponentType.AMMUNITION, AmmunitionComponent(balancingSupplier))
	}

	override fun decorateItemStack(base: ItemStack) {
		customComponents.getComponent(CustomComponentType.AMMUNITION).setAmmo(base, this, balancing.capacity)
	}
}
