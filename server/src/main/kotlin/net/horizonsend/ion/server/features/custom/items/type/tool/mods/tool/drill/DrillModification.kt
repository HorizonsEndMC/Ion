package net.horizonsend.ion.server.features.custom.items.type.tool.mods.tool.drill

import net.horizonsend.ion.server.features.custom.items.type.tool.PowerDrill
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ApplicationPredicate
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ItemModification

/**
 * Tool modifications for drills
 **/
abstract class DrillModification : ItemModification, net.horizonsend.ion.server.features.custom.items.type.tool.mods.tool.BlockListModifier {
	override val applicationPredicates: Array<ApplicationPredicate> = arrayOf(ApplicationPredicate.ClassPredicate(PowerDrill::class))

	override val crouchingDisables: Boolean = true
}
