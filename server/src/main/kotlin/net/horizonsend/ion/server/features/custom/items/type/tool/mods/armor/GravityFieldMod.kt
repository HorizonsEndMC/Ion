package net.horizonsend.ion.server.features.custom.items.type.tool.mods.armor

import net.horizonsend.ion.common.extensions.informationAction
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys
import net.horizonsend.ion.server.core.registration.keys.ItemModKeys
import net.horizonsend.ion.server.core.registration.registries.CustomItemRegistry
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.attribute.CustomItemAttribute
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ApplicationPredicate
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ItemModification
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ModificationItem
import net.horizonsend.ion.server.features.world.IonWorld.Companion.hasFlag
import net.horizonsend.ion.server.features.world.WorldFlag
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import java.util.UUID
import java.util.function.Supplier
import kotlin.Array
import kotlin.reflect.KClass

object GravityFieldMod : ItemModification {
	override val applicationPredicates: Array<ApplicationPredicate> = arrayOf(
		ApplicationPredicate.SpecificPredicate(CustomItemKeys.HEAVY_POWER_ARMOR_BOOTS),
		ApplicationPredicate.SpecificPredicate(CustomItemKeys.MEDIUM_POWER_ARMOR_BOOTS),
		ApplicationPredicate.SpecificPredicate(CustomItemKeys.LIGHT_POWER_ARMOR_BOOTS))
	override val incompatibleWithMods: Array<KClass<out ItemModification>> = arrayOf()
	override val modItem: IonRegistryKey<CustomItem, out CustomItem> = CustomItemKeys.ARMOR_MODIFICATION_GRAVITY_FIELD
	override val crouchingDisables: Boolean = false
	val gravityEnabledPlayers = mutableSetOf<UUID>()
	override val key = ItemModKeys.GRAVITY_FIELD
	override val displayName: Component = ofChildren(
		Component.text("Gravity Field", NamedTextColor.RED),
		Component.text(" Module", NamedTextColor.GOLD),
		Component.text(" Boots Module", NamedTextColor.DARK_GRAY)
	)
	override val primaryOrSecondary: ItemModification.PrimaryOrSecondary = ItemModification.PrimaryOrSecondary.PRIMARY
	override fun getAttributes(): List<CustomItemAttribute> = listOf()
	fun setGravity(player : Player) {
		if (!player.world.hasFlag(WorldFlag.SPACE_WORLD))  {
			player.sendMessage("Gravity Boots only work in space!")
			gravityEnabledPlayers.remove(player.uniqueId)
			return
		}

		val isEnabled = gravityEnabledPlayers.contains(player.uniqueId)
		if (isEnabled) gravityEnabledPlayers.remove(player.uniqueId)
		else gravityEnabledPlayers.add(player.uniqueId)
		player.sendMessage(
			"Gravity Module ${if (isEnabled) { "Enabled" } else { "Disabled" }}")
	}

	fun getGravityEnabled(player: Player) : Boolean {
		return gravityEnabledPlayers.contains(player.uniqueId)
	}

	fun forceDisableGravityBoots(player: Player) {
		gravityEnabledPlayers.remove(player.uniqueId)
	}
}
