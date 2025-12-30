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
		Component.text("Adrenaline Boosting", NamedTextColor.RED),
		Component.text(" Module", NamedTextColor.GOLD),
		Component.text(" Chestplate Module", NamedTextColor.DARK_GRAY)
	)
	override val primaryOrSecondary: ItemModification.PrimaryOrSecondary = ItemModification.PrimaryOrSecondary.PRIMARY
	override fun getAttributes(): List<CustomItemAttribute> = listOf()

	fun setLocked(player : Player) {
		val isEnabled = armorLockEnabledPlayers.contains(player.uniqueId)
		if (isEnabled) armorLockEnabledPlayers.remove(player.uniqueId)
		else armorLockEnabledPlayers[player.uniqueId] = System.nanoTime()
		player.sendMessage(
			"Armor Lock Module ${if (isEnabled) { "Enabled" } else { "Disabled" }}")
	}

	fun getLockEnabled(player: Player) : Boolean {
		return armorLockEnabledPlayers.contains(player.uniqueId)
	}

	fun forceDisableArmorLock(player: Player) {
		if (armorLockEnabledPlayers.contains(player.uniqueId)) {
			armorLockEnabledPlayers.remove(player.uniqueId)
			player.isInvulnerable = false
			player.canPickupItems = true
			player.sendMessage("Armor Lock Module Disabled")
		}
	}

	fun spawnAura(player: Player) {
		val origin = player.location
		val vector = Vector(0, 1, 0)
		for (point in helixAroundVector(origin, vector, 0.45, 10, wavelength = 0.5))
			point.world.spawnParticle(Particle.SOUL_FIRE_FLAME, point.x, point.y, point.z, 1, 0.0, 0.0, 0.0, 0.0, null, true)
	}
}
