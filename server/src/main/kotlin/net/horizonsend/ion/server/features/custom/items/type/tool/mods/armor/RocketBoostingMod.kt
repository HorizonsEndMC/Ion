package net.horizonsend.ion.server.features.custom.items.type.tool.mods.armor

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys
import net.horizonsend.ion.server.core.registration.keys.ItemModKeys
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.attribute.CustomItemAttribute
import net.horizonsend.ion.server.features.custom.items.type.armor.AscendingMode
import net.horizonsend.ion.server.features.custom.items.type.armor.StrafingMode
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ApplicationPredicate
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ItemModification
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.GOLD
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import org.bukkit.entity.Player
import java.util.UUID
import kotlin.reflect.KClass

object RocketBoostingMod : ItemModification {
	override val key = ItemModKeys.ROCKET_BOOSTING
	override val applicationPredicates: Array<ApplicationPredicate> = arrayOf(ApplicationPredicate.SpecificPredicate(CustomItemKeys.HEAVY_POWER_ARMOR_BOOTS),
		ApplicationPredicate.SpecificPredicate(CustomItemKeys.MEDIUM_POWER_ARMOR_BOOTS),
		ApplicationPredicate.SpecificPredicate(CustomItemKeys.LIGHT_POWER_ARMOR_BOOTS))
	override val incompatibleWithMods: Array<KClass<out ItemModification>> = arrayOf()
	override val modItem: IonRegistryKey<CustomItem, out CustomItem> = CustomItemKeys.ARMOR_MODIFICATION_ROCKET_BOOSTING
	override val crouchingDisables: Boolean = false
	override val displayName: Component = ofChildren(		Component.text("Rocket Boosting", NamedTextColor.RED),
		Component.text(" Module", NamedTextColor.GOLD),
		Component.text(" Boots Module", NamedTextColor.DARK_GRAY)
	)
	override val primaryOrSecondary: ItemModification.PrimaryOrSecondary = ItemModification.PrimaryOrSecondary.PRIMARY

	override fun getAttributes(): List<CustomItemAttribute> = listOf()

	val glidingPlayers = mutableSetOf<UUID>()
	val glideDisabledPlayers = mutableMapOf<UUID, Long>() // UUID to end time of glide block
	val strafingMode= mutableMapOf<UUID, StrafingMode>()
	val ascendingMode = mutableMapOf<UUID, AscendingMode>()

	fun setGliding(player: Player, gliding: Boolean) {
		if (gliding) enableGliding(player) else disableGliding(player)
	}

	private fun enableGliding(player: Player) {
		@Suppress("DEPRECATION") // Any other method would cause weirdness not allow low flight
		if (player.isOnGround) return
		glidingPlayers.add(player.uniqueId)
		player.isGliding = true
	}

	private fun disableGliding(player: Player) {
		glidingPlayers.remove(player.uniqueId)
		player.isGliding = false
	}
}
