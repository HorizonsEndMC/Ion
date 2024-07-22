package net.horizonsend.ion.server.features.custom.items.mods.general

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.mods.ItemModification
import net.horizonsend.ion.server.features.custom.items.mods.items.ModificationItem
import net.horizonsend.ion.server.features.custom.items.powered.PowerChainsaw
import net.horizonsend.ion.server.features.custom.items.powered.PowerDrill
import net.horizonsend.ion.server.features.custom.items.powered.PowerHoe
import net.horizonsend.ion.server.features.machine.PowerMachines
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
import net.kyori.adventure.text.format.TextDecoration
import java.util.function.Supplier
import kotlin.reflect.KClass

class PowerCapacityIncrease(
	val increaseAmount: Int,
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
}
