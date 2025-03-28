package net.horizonsend.ion.server.features.custom.items.type.tool.mods.armor

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.core.registries.IonRegistryKey
import net.horizonsend.ion.server.core.registries.keys.CustomItemKeys
import net.horizonsend.ion.server.core.registries.keys.ItemModKeys
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.attribute.CustomItemAttribute
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ApplicationPredicate
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ItemModification
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor.GOLD
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import kotlin.reflect.KClass

object ShockAbsorbingMod : ItemModification {
	override val key = ItemModKeys.SHOCK_ABSORBING
	override val applicationPredicates: Array<ApplicationPredicate> = arrayOf(ApplicationPredicate.SpecificPredicate(CustomItemKeys.POWER_ARMOR_CHESTPLATE))
	override val incompatibleWithMods: Array<KClass<out ItemModification>> = arrayOf()
	override val modItem: IonRegistryKey<CustomItem, out CustomItem> = CustomItemKeys.ARMOR_MODIFICATION_SHOCK_ABSORBING
	override val crouchingDisables: Boolean = false
	override val displayName: Component = ofChildren(Component.text("Shock Absorbing", GRAY), Component.text(" Module", GOLD))

	override fun getAttributes(): List<CustomItemAttribute> = listOf()
}
