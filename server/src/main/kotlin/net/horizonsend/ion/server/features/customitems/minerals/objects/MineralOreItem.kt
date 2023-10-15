package net.horizonsend.ion.server.features.customitems.minerals.objects

import net.horizonsend.ion.server.features.customitems.CustomBlockItem
import net.kyori.adventure.text.Component
import org.bukkit.Material

abstract class MineralOreItem(
    identifier: String,

    material: Material,
    customModelData: Int,
    displayName: Component
) : CustomBlockItem(identifier, material, customModelData, displayName) {
}