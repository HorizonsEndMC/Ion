package net.horizonsend.ion.server.features.custom.items.mods.general

import net.horizonsend.ion.common.utils.text.ITALIC
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.custom.CustomItem
import net.horizonsend.ion.server.features.custom.CustomItemRegistry
import net.horizonsend.ion.server.features.custom.items.attribute.CustomItemAttribute
import net.horizonsend.ion.server.features.custom.items.mods.ItemModification
import net.horizonsend.ion.server.features.custom.items.mods.ModificationItem
import net.horizonsend.ion.server.features.custom.items.powered.PowerChainsaw
import net.horizonsend.ion.server.features.custom.items.powered.PowerHoe
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import java.util.function.Supplier
import kotlin.reflect.KClass

object AutoReplantModifier: ItemModification {
	override val identifier: String = "AUTO_REPLANT"
	override val displayName: Component = ofChildren(text("Auto ", GRAY), text("Replant", GREEN)).decoration(ITALIC, false)

	override val applicableTo: Array<KClass<out CustomItem>> = arrayOf(PowerChainsaw::class, PowerHoe::class)
	override val incompatibleWithMods: Array<KClass<out ItemModification>> = arrayOf()
	override val modItem: Supplier<ModificationItem?> = Supplier { CustomItemRegistry.AUTO_REPLANT }

	// Just a range extender for something that already vein mines
	override val crouchingDisables: Boolean = true

	override fun getAttributes(): List<CustomItemAttribute> = listOf()
}
