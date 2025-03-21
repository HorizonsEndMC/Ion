package net.horizonsend.ion.server.features.custom.items.type

import net.horizonsend.ion.server.core.registries.IonRegistryKey
import net.horizonsend.ion.server.features.custom.blocks.CustomBlock
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory
import net.kyori.adventure.text.Component
import org.bukkit.Material

open class CustomBlockItem(
	identifier: IonRegistryKey<CustomItem, out CustomItem>,

	val customModel: String,
	displayName: Component,

	val customBlockKey: IonRegistryKey<CustomBlock, out CustomBlock>
) : CustomItem(
	identifier,
	displayName,
	ItemFactory
		.builder(ItemFactory.stackableCustomItem)
		.setMaterial(Material.WARPED_WART_BLOCK)
		.setCustomModel(customModel)
		.build()
) {
    fun getCustomBlock(): CustomBlock {
        return customBlockKey.getValue()
    }
}
