package net.horizonsend.ion.server.features.custom.items.type

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_GRAY
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.customItem
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.horizonsend.ion.server.miscellaneous.utils.updatePersistentDataContainer
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.text.DecimalFormat

object ProgressHolder : CustomItem(
	"PROGRESS_HOLDER",
	text("Empty Progress Holder"),
	ItemFactory.unStackableCustomItem
) {
	private val percentFormat = DecimalFormat("##.##%")

	fun create(result: CustomItem): ItemStack {
		val example = result.constructItemStack()
		val base = ItemStack(example.type)

		base.setData(DataComponentTypes.CUSTOM_NAME, ofChildren(text("Assembling ", GRAY, TextDecoration.ITALIC), example.displayName()))
		base.setData(DataComponentTypes.LORE, ItemLore.lore(listOf(
			text("Progress: ", GRAY, TextDecoration.ITALIC),
			text(percentFormat.format(0.0), HE_LIGHT_GRAY, TextDecoration.ITALIC)
		)))
		example.getData(DataComponentTypes.ITEM_MODEL)?.let { base.setData(DataComponentTypes.ITEM_MODEL, it) }

		return base.updatePersistentDataContainer {
			set(NamespacedKeys.CUSTOM_ITEM, PersistentDataType.STRING, identifier)
			set(NamespacedKeys.PROGRESS, PersistentDataType.DOUBLE, 0.0)
			set(NamespacedKeys.CUSTOM_ITEM_RESULT, PersistentDataType.STRING, result.identifier)
		}
	}

	/**
	 * Get the current progress amount
	 **/
	fun getProgress(itemStack: ItemStack): Double {
		return itemStack.itemMeta.persistentDataContainer.getOrDefault(NamespacedKeys.PROGRESS, PersistentDataType.DOUBLE, 0.0)
	}

	/**
	 * Get the custom item result of this progress item
	 **/
	fun getResult(itemStack: ItemStack): CustomItem? {
		val identifier = itemStack.itemMeta.persistentDataContainer.get(NamespacedKeys.CUSTOM_ITEM_RESULT, PersistentDataType.STRING) ?: return null
		return CustomItemRegistry.getByIdentifier(identifier)
	}

	/**
	 * Sets the progress of this custom item
	 *
	 * @return whether the item was completed
	 **/
	fun setProgress(itemStack: ItemStack, progress: Double): Boolean {
		if (itemStack.customItem !is ProgressHolder) return false

		if (progress >= 1.0) return run {
			complete(itemStack)
			true
		}

		itemStack
			.updateLore(listOf(ofChildren(
				text("Progress: ", GRAY, TextDecoration.ITALIC),
				text(percentFormat.format(progress), HE_LIGHT_GRAY, TextDecoration.ITALIC)
			)))
			.updatePersistentDataContainer {
				set(NamespacedKeys.PROGRESS, PersistentDataType.DOUBLE, progress)
			}

		return false
	}

	/**
	 * Mutates this ItemStack into the result custom item
	 **/
	fun complete(itemStack: ItemStack) {
		val result = getResult(itemStack) ?: return
		val example = result.constructItemStack()

		itemStack.itemMeta = example.itemMeta
	}
}
