package net.horizonsend.ion.server.features.custom.items.mods

import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.mods.items.ModificationItem
import net.kyori.adventure.text.Component
import java.util.function.Supplier
import kotlin.reflect.KClass

interface ItemModification {
	val identifier: String
	val displayName: Component
	val applicableTo: Array<KClass<out CustomItem>>
	val incompatibleWithMods: Array<KClass<out ItemModification>>
	val modItem: Supplier<ModificationItem?>
}
