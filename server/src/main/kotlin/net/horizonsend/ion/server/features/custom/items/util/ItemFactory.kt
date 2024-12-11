package net.horizonsend.ion.server.features.custom.items.util

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys.HORIZONSEND_NAMESPACE
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.function.Consumer
import java.util.function.Supplier

class ItemFactory private constructor(
	val material: Material,
	val customModel: String?,
	val maxStackSize: Int?,
	val nameSupplier: Supplier<Component>?,
	val loreSupplier: ((ItemStack) -> List<Component>)?,
	val itemModifiers: List<Consumer<ItemStack>>
) {
	fun construct(): ItemStack {
		val base = ItemStack(material)

		if (customModel != null) base.setData(DataComponentTypes.ITEM_MODEL, Key.key(HORIZONSEND_NAMESPACE, customModel))
		if (maxStackSize != null) base.setData(DataComponentTypes.MAX_STACK_SIZE, maxStackSize)
		if (nameSupplier != null) base.setData(DataComponentTypes.CUSTOM_NAME, nameSupplier.get())
		if (loreSupplier != null) base.setData(DataComponentTypes.LORE, ItemLore.lore(loreSupplier.invoke(base)))

		itemModifiers.forEach { it.accept(base) }

		return base
	}

	fun construct(modifier: Consumer<ItemStack>) {
		val base = construct()
		modifier.accept(base)
	}

	class Builder() {
		constructor(from: ItemFactory) : this() {
			this.material = from.material
			this.customModel = from.customModel
			this.maxStackSize = from.maxStackSize
			this.nameSupplier = from.nameSupplier
			this.loreSupplier = from.loreSupplier
			this.itemModifiers = from.itemModifiers.toMutableList()
		}

		private var material = Material.WARPED_FUNGUS_ON_A_STICK
		private var customModel: String? = null
		private var maxStackSize: Int? = null
		private var nameSupplier: Supplier<Component>? = null
		private var loreSupplier: ((ItemStack) -> List<Component>)? = null
		private var itemModifiers: MutableList<Consumer<ItemStack>> = mutableListOf()

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

		fun setLoreSupplier(loreSupplier: (ItemStack) -> List<Component>): Builder {
			this.loreSupplier = loreSupplier
			return this
		}

		fun addModifier(modifier: Consumer<ItemStack>): Builder {
			this.itemModifiers += modifier
			return this
		}

		fun <T : Any> addPDCEntry(key: NamespacedKey, type: PersistentDataType<*, T>, value: T): Builder {
			return addModifier { item -> item.updateMeta { it.persistentDataContainer.set(key, type, value) } }
		}

		fun build(): ItemFactory {
			return ItemFactory(
				material = this.material,
				customModel = this.customModel,
				maxStackSize = this.maxStackSize,
				nameSupplier = this.nameSupplier,
				loreSupplier = this.loreSupplier,
				itemModifiers = this.itemModifiers,
			)
		}
	}

	companion object Preset {
		fun builder() = Builder()
		fun builder(from: ItemFactory) = Builder(from)

		fun ItemFactory.withModel(model: String) = builder(this).setCustomModel(model).build()

		val stackableCustomItem = builder()
			.setMaterial(Material.WARPED_FUNGUS_ON_A_STICK)
			.setMaxStackSize(64)
			.build()

		val unStackableCustomItem = builder()
			.setMaterial(Material.WARPED_FUNGUS_ON_A_STICK)
			.setMaxStackSize(1)
			.build()

		fun stackableCustomItem(material: Material = Material.WARPED_FUNGUS_ON_A_STICK, maxStackSize: Int = 64, model: String) = builder(stackableCustomItem).setMaterial(material).setMaxStackSize(maxStackSize).setCustomModel(model).build()
		fun unStackableCustomItem(model: String) = builder(stackableCustomItem).setCustomModel(model).build()
	}
}
