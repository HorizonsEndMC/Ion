package net.horizonsend.ion.server.features.custom.items.type.food

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.Consumable
import io.papermc.paper.datacomponent.item.FoodProperties
import io.papermc.paper.datacomponent.item.consumable.ConsumeEffect
import io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation
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
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.ItemStack

class FoodItem(
    key: IonRegistryKey<CustomItem, FoodItem>,
    displayName: Component,
    itemModel: String,
    stackSize: Int,
    hunger: Int,
    saturation: Float,
    consumeSeconds: Float = 1.6f,
    sound: Key = Key.key("minecraft", "entity.generic.eat"),
    consumeEffects: MutableList<ConsumeEffect> = mutableListOf<ConsumeEffect>(),
    canAlwaysEat: Boolean = false,
    lore: List<Component> = listOf(),
    callback: (PlayerItemConsumeEvent, FoodItem, ItemStack) -> Unit = { _, _, _ -> }
) : CustomItem(
    key,
    displayName,
    ItemFactory
        .builder()
        .setMaterial(Material.BREAD)
        .setCustomModel(itemModel)
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
            .saturation(saturation)
            .nutrition(hunger)
            .canAlwaysEat(canAlwaysEat)
            .build()
        )
        .build()
) {
    val loreComponent = FlavorText(lore)

    override val customComponents: CustomItemComponentManager = CustomItemComponentManager(serializationManager).apply {
        addComponent(CustomComponentTypes.LISTENER_PLAYER_CONSUME, playerConsumeListener(this@FoodItem) { event, foodItem, itemStack ->
            callback(event, foodItem, itemStack)
        })
        addComponent(CustomComponentTypes.FLAVOR_TEXT, loreComponent)
    }
}