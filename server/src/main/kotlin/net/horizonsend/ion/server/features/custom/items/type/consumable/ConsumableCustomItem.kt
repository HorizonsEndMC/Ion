package net.horizonsend.ion.server.features.custom.items.type.consumable

import net.horizonsend.ion.server.configuration.PVPBalancingConfiguration
import net.horizonsend.ion.server.configuration.PVPBalancingConfiguration.Throwables.ThrowableBalancing
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.component.CustomComponentTypes
import net.horizonsend.ion.server.features.custom.items.component.CustomItemComponentManager
import net.horizonsend.ion.server.features.custom.items.component.Listener
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack
import java.util.function.Supplier

abstract class ConsumableCustomItem(
	key: IonRegistryKey<CustomItem, out CustomItem>,
	itemFactory: ItemFactory,
	displayName: Component,
	private val balancingSupplier: Supplier<PVPBalancingConfiguration.Consumables.ConsumableBalancing>
) : CustomItem(
	key,
	displayName,
	itemFactory
) {
	val balancing get() = balancingSupplier.get()
	val material = Material.WARPED_FUNGUS_ON_A_STICK
	override val customComponents: CustomItemComponentManager = CustomItemComponentManager(serializationManager).apply {
		addComponent(
			CustomComponentTypes.LISTENER_PLAYER_INTERACT,
			Listener.rightClickListener(this@ConsumableCustomItem) { event, _, itemStack ->
				consume(itemStack, event.player)
			})
	}
	open fun consume(itemStack: ItemStack, livingEntity: LivingEntity){}
}
