package net.horizonsend.ion.server.features.custom.items.mods

import net.horizonsend.ion.server.features.custom.items.objects.ModdedCustomItem
import net.kyori.adventure.text.Component
import java.util.function.Supplier
import kotlin.reflect.KClass

interface ItemModification {
	val identifier: String
	val displayName: Component
	val applicableTo: Array<KClass<out ModdedCustomItem>>
	val incompatibleWithMods: Array<KClass<out ItemModification>>
	val modItem: Supplier<ModificationItem?>

	val crouchingDisables: Boolean
}
