package net.horizonsend.ion.server.features.custom.items.type.tool.mods.armor

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry
import net.horizonsend.ion.server.features.custom.items.attribute.CustomItemAttribute
import net.horizonsend.ion.server.features.custom.items.attribute.PotionEffectAttribute
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ApplicationPredicate
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ItemModification
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ModificationItem
import net.horizonsend.ion.server.features.world.IonWorld.Companion.hasFlag
import net.horizonsend.ion.server.features.world.WorldFlag
import net.horizonsend.ion.server.listener.gear.hasMovedInLastSecond
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor.GOLD
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffectType.SPEED
import java.util.function.Supplier
import kotlin.reflect.KClass

object SpeedBoostingMod : ItemModification {
	override val applicationPredicates: Array<ApplicationPredicate> = arrayOf(ApplicationPredicate.SpecificPredicate(CustomItemRegistry.POWER_ARMOR_LEGGINGS))
	override val incompatibleWithMods: Array<KClass<out ItemModification>> = arrayOf()
	override val modItem: Supplier<ModificationItem?> = Supplier { CustomItemRegistry.ARMOR_MODIFICATION_SPEED_BOOSTING }
	override val crouchingDisables: Boolean = false
	override val identifier: String = "SPEED_BOOSTING"
	override val displayName: Component = ofChildren(Component.text("Speed Boosting", GRAY), Component.text(" Module", GOLD))

	override fun getAttributes(): List<CustomItemAttribute> = listOf(PotionEffectAttribute(SPEED, 60, 2, 1) { entity, _, _ ->
		entity is Player
			&& hasMovedInLastSecond(entity)
			&& !entity.world.hasFlag(WorldFlag.ARENA)
	})
}
