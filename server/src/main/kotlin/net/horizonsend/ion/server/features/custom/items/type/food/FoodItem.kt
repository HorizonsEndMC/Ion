package net.horizonsend.ion.server.features.custom.items.type.food

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.Consumable
import io.papermc.paper.datacomponent.item.FoodProperties
import io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.bukkit.Material

class FoodItem(
    key: IonRegistryKey<CustomItem, FoodItem>,
    displayName: Component,
    itemModel: String,
    stackSize: Int,
    hunger: Int,
    saturation: Float,
) : CustomItem(
    key,
    displayName,
    ItemFactory
        .builder()
        .setMaterial(Material.BREAD)
        .setCustomModel(itemModel)
        .setMaxStackSize(stackSize)
        .addData(DataComponentTypes.CONSUMABLE, Consumable.consumable()
            .consumeSeconds(1.6f)
            .animation(ItemUseAnimation.EAT)
            .sound(Key.key("minecraft", "entity.generic.eat"))
            .hasConsumeParticles(true)
            .build()
        )
        .addData(DataComponentTypes.FOOD, FoodProperties.food()
            .saturation(saturation)
            .nutrition(hunger)
            .build()
        )
        .build()
) {
}