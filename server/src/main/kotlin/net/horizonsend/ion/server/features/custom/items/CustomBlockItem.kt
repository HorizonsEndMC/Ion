package net.horizonsend.ion.server.features.custom.items

import net.horizonsend.ion.server.features.custom.CustomItem
import net.horizonsend.ion.server.features.custom.blocks.CustomBlock
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory
import net.kyori.adventure.text.Component
import org.bukkit.Material
import java.util.function.Supplier

open class CustomBlockItem(
	identifier: String,

	material: Material,
	customModel: String,
	displayName: Component,

	private val customBlockSupplier: Supplier<CustomBlock>
) : CustomItem(
	identifier,
	displayName,
	ItemFactory
		.builder(ItemFactory.stackableCustomItem)
		.setCustomModel(customModel)
		.setMaterial(material)
		.build()
) {
    fun getCustomBlock(): CustomBlock {
        return customBlockSupplier.get()
    }
}
