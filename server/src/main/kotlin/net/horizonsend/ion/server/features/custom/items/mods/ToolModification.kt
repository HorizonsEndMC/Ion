package net.horizonsend.ion.server.features.custom.items.mods

import net.horizonsend.ion.server.features.custom.items.CustomItem
import kotlin.reflect.KClass

interface ToolModification {
	val applicableTo: Array<KClass<out CustomItem>>
}
