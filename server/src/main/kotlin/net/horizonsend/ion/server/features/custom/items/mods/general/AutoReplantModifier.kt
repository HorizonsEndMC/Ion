package net.horizonsend.ion.server.features.custom.items.mods.general

import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.mods.ItemModification
import net.horizonsend.ion.server.features.custom.items.mods.ModificationItem
import net.horizonsend.ion.server.features.custom.items.powered.PowerChainsaw
import net.horizonsend.ion.server.features.custom.items.powered.PowerHoe
import net.kyori.adventure.text.Component
import java.util.function.Supplier
import kotlin.reflect.KClass

object AutoReplantModifier: ItemModification {
	override val identifier: String = "AUTO_REPLANT"
	override val displayName: Component = Component.text("Auto Replant") //TODO

	override val applicableTo: Array<KClass<out CustomItem>> = arrayOf(PowerChainsaw::class, PowerHoe::class)
	override val incompatibleWithMods: Array<KClass<out ItemModification>> = arrayOf()
	override val modItem: Supplier<ModificationItem?> = Supplier { null } //TODO

	// Just a range extender for something that already vein mines
	override val crouchingDisables: Boolean = true
}
