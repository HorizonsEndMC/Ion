package net.horizonsend.ion.server.features.custom.items.mods.tool.drill

import net.horizonsend.ion.server.features.custom.CustomItem
import net.horizonsend.ion.server.features.custom.items.mods.ItemModification
import net.horizonsend.ion.server.features.custom.items.mods.tool.BlockListModifier
import net.horizonsend.ion.server.features.custom.items.powered.PowerDrill
import kotlin.reflect.KClass

/**
 * Tool modifications for drills
 **/
abstract class DrillModification : ItemModification, BlockListModifier {
	override val applicableTo: Array<KClass<out CustomItem>> = arrayOf(PowerDrill::class)

	override val crouchingDisables: Boolean = true
}
