package net.horizonsend.ion.server.features.custom.items.mods.types

import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.mods.ToolModification
import net.horizonsend.ion.server.features.custom.items.powered.PowerDrill
import kotlin.reflect.KClass

/**
 * Tool modifications for drills
 **/
abstract class DrillModification : ToolModification, BlockModifier {
	override val applicableTo: Array<KClass<out CustomItem>> = arrayOf(PowerDrill::class)
}
