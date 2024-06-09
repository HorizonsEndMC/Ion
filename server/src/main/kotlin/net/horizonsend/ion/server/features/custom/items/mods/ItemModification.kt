package net.horizonsend.ion.server.features.custom.items.mods

import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.kyori.adventure.text.Component
import kotlin.reflect.KClass

interface ItemModification {
	val identifier: String
	val displayName: Component
	val applicableTo: Array<KClass<out CustomItem>>
	val incompatibleWithMods: Array<KClass<out ItemModification>>
}
