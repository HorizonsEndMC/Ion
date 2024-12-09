package net.horizonsend.ion.server.features.custom.items.blasters

import net.horizonsend.ion.server.configuration.PVPBalancingConfiguration.EnergyWeapons.AmmoStorage
import net.horizonsend.ion.server.features.custom.NewCustomItem
import net.horizonsend.ion.server.features.custom.items.components.AmmunitionComponent
import net.horizonsend.ion.server.features.custom.items.components.CustomItemComponent
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory
import net.kyori.adventure.text.Component
import org.bukkit.inventory.ItemStack
import java.util.function.Supplier

class NewMagazine(identifier: String, displayName: Component, itemFactory: ItemFactory, private val balancingSupplier: Supplier<AmmoStorage>) : NewCustomItem(
	identifier,
	displayName,
	itemFactory,
) {
	val balancing get() = balancingSupplier.get()

	val ammoComponent = AmmunitionComponent(balancingSupplier)
	override val customComponents: List<CustomItemComponent> = listOf(ammoComponent)

	override fun decorateItemStack(base: ItemStack) {
		ammoComponent.setAmmo(base, this, balancing.capacity)
	}
}
