package net.horizonsend.ion.server.features.custom.items.type.consumable

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.Consumable
import io.papermc.paper.datacomponent.item.FoodProperties
import io.papermc.paper.datacomponent.item.consumable.ConsumeEffect
import io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation
import net.horizonsend.ion.server.configuration.PVPBalancingConfiguration
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.component.CustomComponentTypes
import net.horizonsend.ion.server.features.custom.items.component.CustomItemComponentManager
import net.horizonsend.ion.server.features.custom.items.component.FlavorText
import net.horizonsend.ion.server.features.custom.items.component.Listener.Companion.playerConsumeListener
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.bukkit.potion.PotionEffect
import java.util.function.Supplier


class ConsumableItem(
	key: IonRegistryKey<CustomItem, ConsumableItem>,
	displayName: Component,
	stackSize: Int,
	consumeSeconds: Float,
	sound: Key = Key.key("minecraft", "entity.generic.eat"),
	consumeEffects: MutableList<ConsumeEffect> = mutableListOf<ConsumeEffect>(),
	canAlwaysEat: Boolean = true,
	consumeCooldown: Int,
	lore: List<Component> = listOf(),
	balancingSupplier: Supplier<PVPBalancingConfiguration.Consumables.ConsumableBalancing>
) : CustomItem(
	key,
	displayName,
	ItemFactory
		.builder()
		.setMaterial(Material.SWEET_BERRIES)
		.setMaxStackSize(stackSize)
		.addData(DataComponentTypes.CONSUMABLE, Consumable.consumable()
			.consumeSeconds(consumeSeconds)
			.animation(ItemUseAnimation.EAT)
			.sound(sound)
			.hasConsumeParticles(true)
			.addEffects(consumeEffects)
			.build()
		)
		.addData(DataComponentTypes.FOOD, FoodProperties.food()
			.canAlwaysEat(canAlwaysEat)
			.build()
		)
		.build()
) {
	val loreComponent = FlavorText(lore)
	val balancing = balancingSupplier.get()

	override val customComponents: CustomItemComponentManager = CustomItemComponentManager(serializationManager).apply {
		addComponent(CustomComponentTypes.LISTENER_PLAYER_CONSUME, playerConsumeListener(this@ConsumableItem) consume@ { event, foodItem, itemStack ->
			val potionEffectType = NamespacedKey.fromString(balancing.potionEffectType)?.let { Registry.EFFECT.getOrThrow(it) }?:return@consume
			val effect = PotionEffect(potionEffectType, balancing.potionEffectDuration, balancing.potionEffectAmplifier)
			val player = event.player
			player.addPotionEffect(effect)
			player.setCooldown(itemStack, balancing.timeBetweenConsumption)
		})
		addComponent(CustomComponentTypes.FLAVOR_TEXT, loreComponent)
	}

}
