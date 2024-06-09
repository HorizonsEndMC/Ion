package net.horizonsend.ion.server.features.custom.items.mods.general

import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.mods.ItemModification
import net.horizonsend.ion.server.features.custom.items.powered.PowerDrill
import net.kyori.adventure.text.Component
import kotlin.reflect.KClass

class PowerCapacityIncrease(
	val increaseAmount: Int,
	override val displayName: Component
) : ItemModification {
	override val identifier: String = "POWER_CAPACITY_$increaseAmount"
	override val applicableTo: Array<KClass<out CustomItem>> = arrayOf(PowerDrill::class)
	override val incompatibleWithMods: Array<KClass<out ItemModification>> = arrayOf()
}
