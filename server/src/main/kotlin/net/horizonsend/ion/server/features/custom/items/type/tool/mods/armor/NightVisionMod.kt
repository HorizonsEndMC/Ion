package net.horizonsend.ion.server.features.custom.items.type.tool.mods.armor

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.core.registries.IonRegistryKey
import net.horizonsend.ion.server.core.registries.keys.CustomItemKeys
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.attribute.CustomItemAttribute
import net.horizonsend.ion.server.features.custom.items.attribute.PotionEffectAttribute
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ApplicationPredicate
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ItemModification
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor.GOLD
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import org.bukkit.potion.PotionEffectType.NIGHT_VISION
import kotlin.reflect.KClass

object NightVisionMod : ItemModification {
	override val applicationPredicates: Array<ApplicationPredicate> = arrayOf(ApplicationPredicate.SpecificPredicate(CustomItemKeys.POWER_ARMOR_HELMET))
	override val incompatibleWithMods: Array<KClass<out ItemModification>> = arrayOf(EnvironmentMod::class, PressureFieldMod::class)
	override val modItem: IonRegistryKey<CustomItem, out CustomItem> = CustomItemKeys.ARMOR_MODIFICATION_NIGHT_VISION
	override val crouchingDisables: Boolean = false
	override val identifier: String = "NIGHT_VISION"
	override val displayName: Component = ofChildren(Component.text("Night Vision", GRAY), Component.text(" Module", GOLD))

	override fun getAttributes(): List<CustomItemAttribute> = listOf(PotionEffectAttribute(NIGHT_VISION, 1000, 1, 0) { _, _, _ -> false })
}
