package net.horizonsend.ion.server.features.custom.items.type.tool.mods.general

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.customItem
import net.horizonsend.ion.server.features.custom.items.attribute.AdditionalPowerStorage
import net.horizonsend.ion.server.features.custom.items.attribute.CustomItemAttribute
import net.horizonsend.ion.server.features.custom.items.component.CustomComponentTypes
import net.horizonsend.ion.server.features.custom.items.type.tool.PowerChainsaw
import net.horizonsend.ion.server.features.custom.items.type.tool.PowerDrill
import net.horizonsend.ion.server.features.custom.items.type.tool.PowerHoe
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ItemModification
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ModificationItem
import net.horizonsend.ion.server.features.custom.items.util.updateDurability
import net.horizonsend.ion.server.features.machine.PowerMachines
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.inventory.ItemStack
import java.util.function.Supplier
import kotlin.reflect.KClass

class PowerCapacityIncrease(
	private val increaseAmount: Int,
	override val modItem: Supplier<ModificationItem?>
) : ItemModification {
	override val crouchingDisables: Boolean = false
	override val identifier: String = "POWER_CAPACITY_$increaseAmount"
	override val applicableTo: Array<KClass<out CustomItem>> = arrayOf(PowerDrill::class, PowerHoe::class, PowerChainsaw::class)
	override val incompatibleWithMods: Array<KClass<out ItemModification>> = arrayOf()

	override val displayName: Component = ofChildren(
		text("Power Storage ", RED),
		text("+", YELLOW),
		PowerMachines.prefixComponent,
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
