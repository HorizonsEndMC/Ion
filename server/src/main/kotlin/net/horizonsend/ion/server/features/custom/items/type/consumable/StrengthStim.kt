package net.horizonsend.ion.server.features.custom.items.type.consumable

import net.horizonsend.ion.server.configuration.PVPBalancingConfiguration
import net.horizonsend.ion.server.configuration.PVPBalancingConfiguration.BlasterWeapons.Balancing
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.attribute.PotionEffectAttribute
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
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType.STRENGTH
import java.util.function.Supplier

@Suppress("DEPRECATION")
class StrengthStim(
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
		if (livingEntity.hasPotionEffect(STRENGTH)) return//this is deprecated, but on god I cant find the alternative
		if ((livingEntity as Player).hasCooldown(itemStack)) return
		val effect = PotionEffect(STRENGTH, 2000, balancing.modifierValue.toInt())
		livingEntity.addPotionEffect(effect, true)
		livingEntity.clearActiveItem()
		(livingEntity as? Player)?.setCooldown(itemStack.type, balancing.cooldownTicks)
		itemStack.amount -= 1 //technically uneeded, however if this item were to be stackable we'd want this, as such ive kept it in
		if (itemStack.amount == 0) {
			val inventory = (livingEntity as? InventoryHolder)?.inventory ?: return
			inventory.remove(itemStack)
		}
	}
}
