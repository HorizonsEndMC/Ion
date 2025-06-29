package net.horizonsend.ion.server.features.custom.items.type.tool.mods.tool

import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys
import net.horizonsend.ion.server.core.registration.keys.ItemModKeys
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.attribute.CustomItemAttribute
import net.horizonsend.ion.server.features.custom.items.type.tool.PowerChainsaw
import net.horizonsend.ion.server.features.custom.items.type.tool.PowerDrill
import net.horizonsend.ion.server.features.custom.items.type.tool.PowerHoe
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ApplicationPredicate
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ItemModification
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import kotlin.reflect.KClass

object CollectorModifier : ItemModification {
	override val key: IonRegistryKey<ItemModification, out ItemModification> = ItemModKeys.COLLECTOR
	override val displayName: Component = text("Item Collector", HEColorScheme.HE_LIGHT_ORANGE)
	override val applicationPredicates: Array<ApplicationPredicate> = arrayOf(
		ApplicationPredicate.ClassPredicate(PowerDrill::class),
		ApplicationPredicate.ClassPredicate(PowerHoe::class),
		ApplicationPredicate.ClassPredicate(PowerChainsaw::class)
	)
	override val incompatibleWithMods: Array<KClass<out ItemModification>> = arrayOf()

	override val modItem: IonRegistryKey<CustomItem, out CustomItem>? = CustomItemKeys.TOOL_MODIFICATION_COLLECTOR

	override val crouchingDisables: Boolean = false

	override fun getAttributes(): List<CustomItemAttribute> = listOf()
}
