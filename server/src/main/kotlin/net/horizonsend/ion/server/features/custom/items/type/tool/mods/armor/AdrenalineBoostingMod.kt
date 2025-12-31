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
import net.horizonsend.ion.server.features.world.IonWorld.Companion.hasFlag
import net.horizonsend.ion.server.features.world.WorldFlag
import net.horizonsend.ion.server.listener.gear.hasMovedInLastSecond
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.GOLD
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.potion.PotionEffectType.REGENERATION
import org.bukkit.potion.PotionEffectType.SPEED
import kotlin.reflect.KClass

object AdrenalineBoostingMod : ItemModification {
	override val key = ItemModKeys.ADRENALINE_BOOSTING
	override val applicationPredicates: Array<ApplicationPredicate> = arrayOf(
		ApplicationPredicate.SpecificPredicate(CustomItemKeys.HEAVY_POWER_ARMOR_CHESTPLATE),
		ApplicationPredicate.SpecificPredicate(CustomItemKeys.MEDIUM_POWER_ARMOR_CHESTPLATE),
		ApplicationPredicate.SpecificPredicate(CustomItemKeys.LIGHT_POWER_ARMOR_CHESTPLATE))
	override val incompatibleWithMods: Array<KClass<out ItemModification>> = arrayOf()
	override val modItem: IonRegistryKey<CustomItem, out CustomItem> = CustomItemKeys.ARMOR_MODIFICATION_ADRENALINE_BOOSTING
	override val crouchingDisables: Boolean = false
	override val displayName: Component = ofChildren(		Component.text("Adrenaline Boosting", NamedTextColor.RED),
		Component.text(" Module", NamedTextColor.GOLD),
		Component.text(" Chestplate Module", NamedTextColor.DARK_GRAY)
	)
	override val primaryOrSecondary: ItemModification.PrimaryOrSecondary = ItemModification.PrimaryOrSecondary.SECONDARY

	override fun getAttributes(): List<CustomItemAttribute> = listOf(PotionEffectAttribute(setOf(EquipmentSlot.CHEST), REGENERATION, 60, 0, 0) { entity, _, _ ->
		entity is Player
			&& hasMovedInLastSecond(entity)
			&& !entity.world.hasFlag(WorldFlag.ARENA)
	})
}
