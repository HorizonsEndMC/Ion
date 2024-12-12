package net.horizonsend.ion.server.features.custom.items.type.tool.mods.tool.drill

import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.type.tool.PowerDrill
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ItemModification
import kotlin.reflect.KClass

/**
 * Tool modifications for drills
 **/
abstract class DrillModification : ItemModification, net.horizonsend.ion.server.features.custom.items.type.tool.mods.tool.BlockListModifier {
	override val applicableTo: Array<KClass<out CustomItem>> = arrayOf(PowerDrill::class)

	override val crouchingDisables: Boolean = true
}
