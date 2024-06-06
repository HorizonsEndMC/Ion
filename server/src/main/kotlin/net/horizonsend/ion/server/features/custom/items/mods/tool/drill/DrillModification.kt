package net.horizonsend.ion.server.features.custom.items.mods.tool.drill

import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.mods.ItemModification
import net.horizonsend.ion.server.features.custom.items.mods.tool.BlockModifier
import net.horizonsend.ion.server.features.custom.items.powered.PowerDrill
import kotlin.reflect.KClass

/**
 * Tool modifications for drills
 **/
abstract class DrillModification : ItemModification, BlockModifier {
	override val applicableTo: Array<KClass<out CustomItem>> = arrayOf(PowerDrill::class)

	open val additionalPowerUsage: Int = 0
}
