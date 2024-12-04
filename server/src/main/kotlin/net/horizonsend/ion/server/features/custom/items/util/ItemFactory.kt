package net.horizonsend.ion.server.features.custom.items.util

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.util.function.Supplier

class ItemFactory private constructor(
	val material: Material,
	val customModel: String?,
	val maxStackSize: Int?,
	val nameSupplier: Supplier<Component>?,
) {
	fun construct(): ItemStack {
		val base = ItemStack(material)

		if (customModel != null) base.setData(DataComponentTypes.ITEM_MODEL, Key.key("horizonsend", customModel))
		if (maxStackSize != null) base.setData(DataComponentTypes.MAX_STACK_SIZE, maxStackSize)
		if (nameSupplier != null) base.setData(DataComponentTypes.CUSTOM_NAME, nameSupplier.get())
		if (loreSupplier != null) base.setData(DataComponentTypes.LORE, ItemLore.lore(loreSupplier.get()))

		return base
	}

	class Builder() {
		var material = Material.WARPED_FUNGUS_ON_A_STICK
		var customModel: String? = null
		var maxStackSize: Int? = null
		var nameSupplier: Supplier<Component>? = null
		var loreSupplier: Supplier<List<Component>>? = null

		fun build(): ItemFactory {
			return ItemFactory(
				material = this.material,
				customModel = this.customModel,
				maxStackSize = this.maxStackSize,
				nameSupplier = this.nameSupplier,
				loreSupplier = this.loreSupplier
			)
		}
	}
}
