package net.horizonsend.ion.server.features.transport.filters

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemAttributeModifiers
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.POWER_DRILL_BASIC
import net.horizonsend.ion.server.core.registration.registries.CustomItemRegistry.Companion.customItem
import net.horizonsend.ion.server.features.custom.items.component.CustomComponentTypes
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.updateData
import net.horizonsend.ion.server.miscellaneous.utils.updateDisplayName
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.horizonsend.ion.server.miscellaneous.utils.updatePersistentDataContainer
import net.kyori.adventure.text.Component.text
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

enum class FilterMethod(val icon: ItemStack) {
	STRICT(POWER_DRILL_BASIC.getValue().constructItemStack()
		.updatePersistentDataContainer {
			set(NamespacedKeys.CUSTOM_ITEM, PersistentDataType.STRING, "USELESS")
		}
		.updateData(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP, true)
		.updateData(DataComponentTypes.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.itemAttributes().build())
		.apply {
			POWER_DRILL_BASIC.getValue().getComponent(CustomComponentTypes.POWER_STORAGE).setPower(POWER_DRILL_BASIC.getValue(), this, 25000)
		}
		.updateDisplayName(text("Strict item checks"))
		.updateLore(mutableListOf(text("All item data will be matched.")))
	) {
		override fun matches(data: ItemStack, target: ItemStack): Boolean {
			return data.isSimilar(target)
		}
	},
	LENIENT(POWER_DRILL_BASIC.getValue().constructItemStack()
		.updatePersistentDataContainer {
			set(NamespacedKeys.CUSTOM_ITEM, PersistentDataType.STRING, "USELESS")
		}
		.updateData(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP, true)
		.updateData(DataComponentTypes.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.itemAttributes().build())
		.updateDisplayName(text("Lenient item checks"))
		.updateLore(mutableListOf(text("Only item IDs will be matched.")))
	) {
		override fun matches(data: ItemStack, target: ItemStack): Boolean {
			val customItem = target.customItem
			if (customItem != null) {
				return data.customItem?.key == customItem.key
			}

			return target.type == data.type
		}
	};

	abstract fun matches(data: ItemStack, target: ItemStack): Boolean
}
