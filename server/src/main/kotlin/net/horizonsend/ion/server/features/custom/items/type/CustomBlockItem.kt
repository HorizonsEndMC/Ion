package net.horizonsend.ion.server.features.custom.items.type

import net.horizonsend.ion.server.features.custom.blocks.CustomBlock
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory
import net.kyori.adventure.text.Component
import org.bukkit.Material
import java.util.function.Supplier

open class CustomBlockItem(
	identifier: String,

	customModel: String,
	displayName: Component,

	private val customBlockSupplier: Supplier<CustomBlock>
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
        return customBlockSupplier.get()
    }
}
