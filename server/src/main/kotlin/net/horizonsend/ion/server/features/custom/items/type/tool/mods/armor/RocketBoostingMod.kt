package net.horizonsend.ion.server.features.custom.items.type.tool.mods.armor

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry
import net.horizonsend.ion.server.features.custom.items.attribute.CustomItemAttribute
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ApplicationPredicate
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ItemModification
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ModificationItem
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor.GOLD
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import org.bukkit.entity.Player
import java.util.UUID
import java.util.function.Supplier
import kotlin.reflect.KClass

object RocketBoostingMod : ItemModification {
	override val applicationPredicates: Array<ApplicationPredicate> = arrayOf(ApplicationPredicate.SpecificPredicate(CustomItemRegistry.POWER_ARMOR_BOOTS))
	override val incompatibleWithMods: Array<KClass<out ItemModification>> = arrayOf()
	override val modItem: Supplier<ModificationItem?> = Supplier { CustomItemRegistry.ARMOR_MODIFICATION_ROCKET_BOOSTING }
	override val crouchingDisables: Boolean = false
	override val identifier: String = "ROCKET_BOOSTING"
	override val displayName: Component = ofChildren(Component.text("Rocket Boosting", GRAY), Component.text(" Module", GOLD))

	override fun getAttributes(): List<CustomItemAttribute> = listOf()

	private val glidingPlayers = mutableSetOf<UUID>()
	val glideDisabledPlayers = mutableMapOf<UUID, Long>() // UUID to end time of glide block

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
