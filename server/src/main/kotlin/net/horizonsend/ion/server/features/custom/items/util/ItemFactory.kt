package net.horizonsend.ion.server.features.custom.items.util

import io.papermc.paper.datacomponent.DataComponentTypes
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys.HORIZONSEND_NAMESPACE
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.util.function.Consumer
import java.util.function.Supplier

class ItemFactory private constructor(
	val material: Material,
	val customModel: String?,
	val maxStackSize: Int?,
	val nameSupplier: Supplier<Component>?,
) {
	fun construct(): ItemStack {
		val base = ItemStack(material)

		if (customModel != null) base.setData(DataComponentTypes.ITEM_MODEL, Key.key(HORIZONSEND_NAMESPACE, customModel))
		if (maxStackSize != null) base.setData(DataComponentTypes.MAX_STACK_SIZE, maxStackSize)
		if (nameSupplier != null) base.setData(DataComponentTypes.CUSTOM_NAME, nameSupplier.get())

		return base
	}

	fun construct(modifier: Consumer<ItemStack>) {
		val base = construct()
		modifier.accept(base)
	}

	class Builder {
		private var material = Material.WARPED_FUNGUS_ON_A_STICK
		private var customModel: String? = null
		private var maxStackSize: Int? = null
		private var nameSupplier: Supplier<Component>? = null

		fun setMaterial(material: Material): Builder {
			this.material = material
			return this
		}

		fun setCustomModel(customModel: String): Builder {
			this.customModel = customModel
			return this
		}

		fun setMaxStackSize(maxStackSize: Int): Builder {
			check(maxStackSize < 100)
			this.maxStackSize = maxStackSize
			return this
		}

		fun setNameSupplier(nameSupplier: Supplier<Component>): Builder {
			this.nameSupplier = nameSupplier
			return this
		}

		fun build(): ItemFactory {
			return ItemFactory(
				material = this.material,
				customModel = this.customModel,
				maxStackSize = this.maxStackSize,
				nameSupplier = this.nameSupplier,
			)
		}
	}
}
