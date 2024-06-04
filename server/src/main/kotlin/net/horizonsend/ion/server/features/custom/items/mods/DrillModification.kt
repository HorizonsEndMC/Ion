package net.horizonsend.ion.server.features.custom.items.mods

import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.tool.PowerDrill
import kotlin.reflect.KClass

class DrillModification : ToolModification {
	override val applicableTo: Array<KClass<out CustomItem>> = arrayOf(PowerDrill::class)
}
