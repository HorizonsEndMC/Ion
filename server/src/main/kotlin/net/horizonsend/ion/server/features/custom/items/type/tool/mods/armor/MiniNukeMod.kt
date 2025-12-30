package net.horizonsend.ion.server.features.custom.items.type.tool.mods.armor

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys
import net.horizonsend.ion.server.core.registration.keys.ItemModKeys
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.attribute.CustomItemAttribute
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ApplicationPredicate
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ItemModification
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import kotlin.Array
import kotlin.reflect.KClass

object MiniNukeMod : ItemModification {
	override val applicationPredicates: Array<ApplicationPredicate> = arrayOf(
		ApplicationPredicate.SpecificPredicate(CustomItemKeys.HEAVY_POWER_ARMOR_CHESTPLATE),
		ApplicationPredicate.SpecificPredicate(CustomItemKeys.MEDIUM_POWER_ARMOR_CHESTPLATE),
		ApplicationPredicate.SpecificPredicate(CustomItemKeys.LIGHT_POWER_ARMOR_CHESTPLATE))
	override val incompatibleWithMods: Array<KClass<out ItemModification>> = arrayOf()
	override val modItem: IonRegistryKey<CustomItem, out CustomItem> = CustomItemKeys.ARMOR_MODIFICATION_MINI_NUKE
	override val crouchingDisables: Boolean = false
	override val key = ItemModKeys.MINI_NUKE
	override val displayName: Component = ofChildren(
		Component.text("Mini Nuke", NamedTextColor.RED),
		Component.text(" Module", NamedTextColor.GOLD),
		Component.text(" Chestplate Module", NamedTextColor.DARK_GRAY)
	)
	override val primaryOrSecondary: ItemModification.PrimaryOrSecondary = ItemModification.PrimaryOrSecondary.PRIMARY
	override fun getAttributes(): List<CustomItemAttribute> = listOf()
}
