package net.horizonsend.ion.server.features.custom.items.mods.tool.chainsaw

import net.horizonsend.ion.common.utils.text.miniMessage
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.CustomItems
import net.horizonsend.ion.server.features.custom.items.mods.ItemModification
import net.horizonsend.ion.server.features.custom.items.mods.ModificationItem
import net.horizonsend.ion.server.features.custom.items.powered.PowerChainsaw
import net.kyori.adventure.text.Component
import java.util.function.Supplier
import kotlin.reflect.KClass

object ExtendedBar : ItemModification {
	override val identifier: String = "EXTENDED_BAR"
	override val displayName: Component = "<i><gradient:#a0a0a0:#6c6c6c>Extended Bar</gradient></i>".miniMessage()

	override val applicableTo: Array<KClass<out CustomItem>> = arrayOf(PowerChainsaw::class)
	override val incompatibleWithMods: Array<KClass<out ItemModification>> = arrayOf()
	override val modItem: Supplier<ModificationItem?> = Supplier { CustomItems.EXTENDED_BAR }

	// Just a range extender for something that already vein mines
	override val crouchingDisables: Boolean = false
}
