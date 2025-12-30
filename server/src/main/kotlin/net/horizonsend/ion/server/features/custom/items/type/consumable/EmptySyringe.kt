package net.horizonsend.ion.server.features.custom.items.type.consumable

import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.configuration.PVPBalancingConfiguration
import net.horizonsend.ion.server.configuration.PVPBalancingConfiguration.BlasterWeapons.Balancing
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.component.CustomComponentTypes
import net.horizonsend.ion.server.features.custom.items.component.CustomItemComponentManager
import net.horizonsend.ion.server.features.custom.items.component.Listener
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory
import net.horizonsend.ion.server.features.nations.gui.item
import net.kyori.adventure.text.Component
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityRegainHealthEvent
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import java.util.function.Supplier

@Suppress("DEPRECATION")
class EmptySyringe(
	key: IonRegistryKey<CustomItem, out CustomItem>,
	displayName: Component,
	itemFactory: ItemFactory,
	balancingSupplier: Supplier<PVPBalancingConfiguration.Consumables.ConsumableBalancing>) : ConsumableCustomItem(
		key,
		itemFactory,
		displayName,
		balancingSupplier
	) {
	override fun consume(itemStack: ItemStack, livingEntity: LivingEntity) {
		return livingEntity.userError("Using an empty syringe does nothing!")
	}
}
