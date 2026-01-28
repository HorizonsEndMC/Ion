package net.horizonsend.ion.server.features.custom.items.type.tool.mods.armor

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemAttributeModifiers
import io.papermc.paper.datacomponent.item.TooltipDisplay
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys
import net.horizonsend.ion.server.core.registration.keys.ItemModKeys
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.attribute.CustomItemAttribute
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ApplicationPredicate
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ItemModification
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor.GOLD
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemStack
import kotlin.reflect.KClass

object ShockAbsorbingMod : ItemModification {
	override val key = ItemModKeys.SHOCK_ABSORBING
	override val applicationPredicates: Array<ApplicationPredicate> = arrayOf(ApplicationPredicate.SpecificPredicate(CustomItemKeys.POWER_ARMOR_CHESTPLATE))
	override val incompatibleWithMods: Array<KClass<out ItemModification>> = arrayOf()
	override val modItem: IonRegistryKey<CustomItem, out CustomItem> = CustomItemKeys.ARMOR_MODIFICATION_SHOCK_ABSORBING
	override val crouchingDisables: Boolean = false
	override val displayName: Component = ofChildren(Component.text("Shock Absorbing", GRAY), Component.text(" Module", GOLD))

	override fun getAttributes(): List<CustomItemAttribute> = listOf()

	override fun onAdd(itemStack: ItemStack) {
		val existing = itemStack.getData(DataComponentTypes.ATTRIBUTE_MODIFIERS)

		val builder = ItemAttributeModifiers.itemAttributes()

		if (existing != null) {
			for (modifier in existing.modifiers()) {
				builder.addModifier(modifier.attribute(), modifier.modifier())
			}
		}

		builder.addModifier(
			Attribute.KNOCKBACK_RESISTANCE,
			AttributeModifier(KNOCKBACK_RESISTANCE_KEY, 1.0, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.CHEST)
		)

		itemStack.setData(DataComponentTypes.ATTRIBUTE_MODIFIERS, builder.build())
		itemStack.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().addHiddenComponents(DataComponentTypes.ATTRIBUTE_MODIFIERS))
	}

	override fun onRemove(itemStack: ItemStack) {
		val existing = itemStack.getData(DataComponentTypes.ATTRIBUTE_MODIFIERS) ?: return

		val builder = ItemAttributeModifiers.itemAttributes()
		val trimmed = existing.modifiers().toMutableList()
		trimmed.removeAll { it.modifier().key == KNOCKBACK_RESISTANCE_KEY }

		for (modifier in trimmed) {
			builder.addModifier(modifier.attribute(), modifier.modifier())
		}

		itemStack.setData(DataComponentTypes.ATTRIBUTE_MODIFIERS, builder.build())
		itemStack.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().addHiddenComponents(DataComponentTypes.ATTRIBUTE_MODIFIERS))
	}

	private val KNOCKBACK_RESISTANCE_KEY = NamespacedKeys.key("KNOCKBACK_RESISTANCE")
}
