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
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.alongVector
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.helixAroundVector
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.core.Direction
import org.apache.logging.log4j.core.util.NanoClock
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.Array
import kotlin.reflect.KClass

object ArmorLockMod : ItemModification {
	override val applicationPredicates: Array<ApplicationPredicate> = arrayOf(
		ApplicationPredicate.SpecificPredicate(CustomItemKeys.HEAVY_POWER_ARMOR_LEGGINGS),
		ApplicationPredicate.SpecificPredicate(CustomItemKeys.MEDIUM_POWER_ARMOR_LEGGINGS),
		ApplicationPredicate.SpecificPredicate(CustomItemKeys.LIGHT_POWER_ARMOR_LEGGINGS))
	override val incompatibleWithMods: Array<KClass<out ItemModification>> = arrayOf()
	override val modItem: IonRegistryKey<CustomItem, out CustomItem> = CustomItemKeys.ARMOR_MODIFICATION_ARMOR_LOCK
	override val crouchingDisables: Boolean = false
	override val key = ItemModKeys.ARMOR_LOCK
	val armorLockEnabledPlayers = mutableMapOf<UUID, Long>()
	val maxLockTime = TimeUnit.NANOSECONDS.toNanos(5000000000L)
	override val displayName: Component = ofChildren(
		Component.text("Armor Lock", NamedTextColor.RED),
		Component.text(" Module", NamedTextColor.GOLD),
		Component.text(" Leggings Module", NamedTextColor.DARK_GRAY)
	)
	override val primaryOrSecondary: ItemModification.PrimaryOrSecondary = ItemModification.PrimaryOrSecondary.PRIMARY
	override fun getAttributes(): List<CustomItemAttribute> = listOf()

	fun setLocked(player : Player) {
		val isEnabled = armorLockEnabledPlayers.contains(player.uniqueId)
		if (isEnabled) armorLockEnabledPlayers.remove(player.uniqueId)
		else armorLockEnabledPlayers[player.uniqueId] = System.nanoTime()
	}

	fun getLockEnabled(player: Player) : Boolean {
		return armorLockEnabledPlayers.contains(player.uniqueId)
	}

	fun forceDisableArmorLock(player: Player) {
		if (armorLockEnabledPlayers.contains(player.uniqueId)) {
			armorLockEnabledPlayers.remove(player.uniqueId)
			player.isInvulnerable = false
			player.canPickupItems = true
		}
	}
}
