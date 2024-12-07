package net.horizonsend.ion.server.features.custom.items.blasters

import net.horizonsend.ion.server.configuration.PVPBalancingConfiguration.EnergyWeapons.Balancing
import net.horizonsend.ion.server.features.custom.NewCustomItem
import net.horizonsend.ion.server.features.custom.items.components.AmmunitionComponent
import net.horizonsend.ion.server.features.custom.items.components.CustomItemComponent
import net.horizonsend.ion.server.features.custom.items.components.ListenerComponent
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory
import net.kyori.adventure.text.Component
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack
import java.util.function.Supplier

class NewBlaster<T : Balancing>(
	identifier: String,
	displayName: Component,
	itemFactory: ItemFactory,
	private val balancingSupplier: Supplier<T>
) : NewCustomItem(
	identifier,
	displayName,
	itemFactory,
	1
) {
	override val customComponents: List<CustomItemComponent> = listOf(
		AmmunitionComponent(balancingSupplier),
		ListenerComponent.interactListener(this) { event, _, item -> fire(event.player, item) },
		ListenerComponent.playerSwapHandsListener(this) { event, _, item -> reload(event.player, item) }
	)

	val balancing get() = balancingSupplier.get()

	fun fire(shooter: LivingEntity, blasterItem: ItemStack) {

	}

	fun reload(shooter: LivingEntity, blasterItem: ItemStack) {

	}
}
