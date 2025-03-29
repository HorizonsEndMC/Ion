package net.horizonsend.ion.server.features.custom.items.type.tool.mods.armor

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys
import net.horizonsend.ion.server.core.registration.keys.ItemModKeys
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.attribute.CustomItemAttribute
import net.horizonsend.ion.server.features.custom.items.attribute.PotionEffectAttribute
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ApplicationPredicate
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ItemModification
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor.GOLD
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import org.bukkit.potion.PotionEffectType.WATER_BREATHING
import kotlin.reflect.KClass

object EnvironmentMod : ItemModification {
	override val key = ItemModKeys.ENVIRONMENT
	override val applicationPredicates: Array<ApplicationPredicate> = arrayOf(ApplicationPredicate.SpecificPredicate(CustomItemKeys.POWER_ARMOR_HELMET))
	override val incompatibleWithMods: Array<KClass<out ItemModification>> = arrayOf(NightVisionMod::class, PressureFieldMod::class)
	override val modItem: IonRegistryKey<CustomItem, out CustomItem>? = CustomItemKeys.ARMOR_MODIFICATION_ENVIRONMENT
	override val crouchingDisables: Boolean = false
	override val displayName: Component = ofChildren(Component.text("Environment", GRAY), Component.text(" Module", GOLD))

	override fun getAttributes(): List<CustomItemAttribute> = listOf(PotionEffectAttribute(WATER_BREATHING, 20, 1, 1) { _, _, _ -> true })
}
