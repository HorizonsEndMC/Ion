package net.horizonsend.ion.server.features.custom.items.type.tool.mods.general

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.core.registries.IonRegistryKey
import net.horizonsend.ion.server.features.client.display.modular.display.PowerEntityDisplayModule.Companion.powerPrefix
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.Companion.customItem
import net.horizonsend.ion.server.features.custom.items.attribute.AdditionalPowerStorage
import net.horizonsend.ion.server.features.custom.items.attribute.CustomItemAttribute
import net.horizonsend.ion.server.features.custom.items.component.CustomComponentTypes
import net.horizonsend.ion.server.features.custom.items.type.tool.PowerChainsaw
import net.horizonsend.ion.server.features.custom.items.type.tool.PowerDrill
import net.horizonsend.ion.server.features.custom.items.type.tool.PowerHoe
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ApplicationPredicate
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ItemModification
import net.horizonsend.ion.server.features.custom.items.util.updateDurability
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.inventory.ItemStack
import kotlin.reflect.KClass

class PowerCapacityIncrease(
	override val key: IonRegistryKey<ItemModification, out ItemModification>,
	private val increaseAmount: Int,
	override val modItem: IonRegistryKey<CustomItem, out CustomItem>,
) : ItemModification {
	override val crouchingDisables: Boolean = false
	override val applicationPredicates: Array<ApplicationPredicate> = arrayOf(
		ApplicationPredicate.ClassPredicate(PowerDrill::class),
		ApplicationPredicate.ClassPredicate(PowerHoe::class),
		ApplicationPredicate.ClassPredicate(PowerChainsaw::class)
	)
	override val incompatibleWithMods: Array<KClass<out ItemModification>> = arrayOf()

	override val displayName: Component = ofChildren(
		text("Power Storage ", RED),
		text("+", YELLOW),
		powerPrefix,
		text(increaseAmount, GREEN)
	).decoration(TextDecoration.ITALIC, false)

	override fun onAdd(itemStack: ItemStack) {
		val customItem = itemStack.customItem ?: return
		if (!customItem.hasComponent(CustomComponentTypes.POWER_STORAGE)) return
		val powerManager = customItem.getComponent(CustomComponentTypes.POWER_STORAGE)

		customItem.refreshLore(itemStack)
		updateDurability(itemStack, powerManager.getPower(itemStack), powerManager.getMaxPower(customItem, itemStack))
	}

	override fun onRemove(itemStack: ItemStack) {
		val customItem = itemStack.customItem ?: return
		if (!customItem.hasComponent(CustomComponentTypes.POWER_STORAGE)) return
		val powerManager = customItem.getComponent(CustomComponentTypes.POWER_STORAGE)

		powerManager.setPower(customItem, itemStack, minOf(powerManager.getPower(itemStack), powerManager.getMaxPower(customItem, itemStack)))
	}

	override fun getAttributes(): List<CustomItemAttribute> {
		return listOf(AdditionalPowerStorage(increaseAmount))
	}
}
