package net.horizonsend.ion.server.features.customitems.misc

import net.horizonsend.ion.common.utils.text.bracketed
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_GRAY
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.customitems.CustomItem
import net.horizonsend.ion.server.features.customitems.CustomItems
import net.horizonsend.ion.server.features.customitems.CustomItems.customItem
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.text.DecimalFormat

object ProgressHolder : CustomItem("PROGRESS_HOLDER") {
	override fun constructItemStack(): ItemStack {
		return ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta {
			it.displayName(text("Empty Progress Holder"))
			it.persistentDataContainer.set(NamespacedKeys.CUSTOM_ITEM, PersistentDataType.STRING, identifier)
		}
	}

	private val percentFormat = DecimalFormat("##.##%")

	fun create(result: CustomItem): ItemStack {
		val example = result.constructItemStack()

		return ItemStack(example.type).updateMeta {
			it.displayName(ofChildren(text("Assembling ", GRAY, TextDecoration.ITALIC), bracketed(example.displayName())))
			it.lore(listOf(ofChildren(
				text("Progress: ", GRAY, TextDecoration.ITALIC),
				text(percentFormat.format(0.0), HE_LIGHT_GRAY, TextDecoration.ITALIC)
			)))
			it.persistentDataContainer.set(NamespacedKeys.CUSTOM_ITEM, PersistentDataType.STRING, identifier)
			it.persistentDataContainer.set(NamespacedKeys.PROGRESS, PersistentDataType.DOUBLE, 0.0)
			it.persistentDataContainer.set(NamespacedKeys.CUSTOM_ITEM_RESULT, PersistentDataType.STRING, result.identifier)
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
		return CustomItems.getByIdentifier(identifier)
	}

	/**
	 * Sets the progress of this custom item
	 **/
	fun setProgress(itemStack: ItemStack, progress: Double) {
		if (itemStack.customItem !is ProgressHolder) return

		if (progress >= 1.0) return complete(itemStack)

		itemStack.updateMeta {
			it.lore(listOf(ofChildren(
				text("Progress: ", GRAY, TextDecoration.ITALIC),
				text(percentFormat.format(progress), HE_LIGHT_GRAY, TextDecoration.ITALIC)
			)))
			it.persistentDataContainer.set(NamespacedKeys.PROGRESS, PersistentDataType.DOUBLE, progress)
		}
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
