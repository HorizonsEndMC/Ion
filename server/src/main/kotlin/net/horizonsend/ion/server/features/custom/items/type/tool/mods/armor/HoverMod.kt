package net.horizonsend.ion.server.features.custom.items.type.tool.mods.armor

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys
import net.horizonsend.ion.server.core.registration.keys.ItemModKeys
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.attribute.CustomItemAttribute
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ApplicationPredicate
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ItemModification
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.armor.GravityFieldMod.gravityEnabledPlayers
import net.horizonsend.ion.server.features.world.IonWorld.Companion.hasFlag
import net.horizonsend.ion.server.features.world.WorldFlag
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import java.util.UUID
import java.util.Vector
import kotlin.Array
import kotlin.reflect.KClass

object HoverMod : ItemModification {
	override val applicationPredicates: Array<ApplicationPredicate> = arrayOf(
		ApplicationPredicate.SpecificPredicate(CustomItemKeys.HEAVY_POWER_ARMOR_BOOTS),
		ApplicationPredicate.SpecificPredicate(CustomItemKeys.MEDIUM_POWER_ARMOR_BOOTS),
		ApplicationPredicate.SpecificPredicate(CustomItemKeys.LIGHT_POWER_ARMOR_BOOTS))
	override val incompatibleWithMods: Array<KClass<out ItemModification>> = arrayOf()
	override val modItem: IonRegistryKey<CustomItem, out CustomItem> = CustomItemKeys.ARMOR_MODIFICATION_HOVER
	override val crouchingDisables: Boolean = false
	override val key = ItemModKeys.HOVER
	val hoverEnabledPlayers = mutableSetOf<UUID>()
	override val displayName: Component = ofChildren(
		Component.text("Hover", NamedTextColor.RED),
		Component.text(" Module", NamedTextColor.GOLD),
		Component.text(" Boots Module", NamedTextColor.DARK_GRAY)
	)
	override val primaryOrSecondary: ItemModification.PrimaryOrSecondary = ItemModification.PrimaryOrSecondary.PRIMARY
	override fun getAttributes(): List<CustomItemAttribute> = listOf()
	fun setHover(player : Player) {
		if (player.world.hasFlag(WorldFlag.SPACE_WORLD))  {
			hoverEnabledPlayers.remove(player.uniqueId)
			player.flySpeed = 0.025f
			player.isFlying = false
			return
		}
		val isEnabled = hoverEnabledPlayers.contains(player.uniqueId)
		if (isEnabled) {
			hoverEnabledPlayers.remove(player.uniqueId)
			player.flySpeed = 0.05f
			player.isFlying = false
		}
		else hoverEnabledPlayers.add(player.uniqueId)
	}

	fun getHoverEnabled(player: Player) : Boolean {
		return hoverEnabledPlayers.contains(player.uniqueId)
	}

	fun forceDisableHoverBoots(player: Player) {
		hoverEnabledPlayers.remove(player.uniqueId)
	}


}
