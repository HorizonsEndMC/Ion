package net.horizonsend.ion.server.features.custom.items.mods

import net.horizonsend.ion.server.features.custom.items.CustomItem
import kotlin.reflect.KClass

interface ItemModification {
	val identifier: String
	val applicableTo: Array<KClass<out CustomItem>>
	val incompatibleWithMods: Array<KClass<out ItemModification>>
}
