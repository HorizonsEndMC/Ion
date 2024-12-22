package net.horizonsend.ion.server.features.custom.items.type.tool.mods.armor

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry
import net.horizonsend.ion.server.features.custom.items.attribute.CustomItemAttribute
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ApplicationPredicate
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ItemModification
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ModificationItem
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor.GOLD
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import java.util.function.Supplier
import kotlin.reflect.KClass

object PressureFieldMod : ItemModification {
	override val applicationPredicates: Array<ApplicationPredicate> = arrayOf(ApplicationPredicate.SpecificPredicate(CustomItemRegistry.POWER_ARMOR_HELMET))
	override val incompatibleWithMods: Array<KClass<out ItemModification>> = arrayOf(EnvironmentMod::class, EnvironmentMod::class)
	override val modItem: Supplier<ModificationItem?> = Supplier { CustomItemRegistry.ARMOR_MODIFICATION_PRESSURE_FIELD }
	override val crouchingDisables: Boolean = false
	override val identifier: String = "PRESSURE_FIELD"
	override val displayName: Component = ofChildren(Component.text("Pressure Field", GRAY), Component.text(" Module", GOLD))

	override fun getAttributes(): List<CustomItemAttribute> = listOf()
}
