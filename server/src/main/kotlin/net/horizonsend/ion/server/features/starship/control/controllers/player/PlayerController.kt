package net.horizonsend.ion.server.features.starship.control.controllers.player

import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.control.controllers.Controller
import net.horizonsend.ion.server.features.starship.damager.PlayerDamager
import net.horizonsend.ion.server.features.starship.damager.damager
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.block.Block
import org.bukkit.block.BlockState
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.EquipmentSlot

abstract class PlayerController(
	final override val player: Player,
	starship: ActiveStarship, name: String
) : Controller(player.damager(starship), starship, name), PlayerDamager {
	override val pilotName: Component = player.displayName()

	override val yaw: Float get() = player.location.yaw
	override val pitch: Float get() = player.location.pitch

	override val color: Color
		 get() = PlayerCache[player].nationOid?.let { Color.fromRGB( NationCache[it].color ) } ?: super.color

	override fun canDestroyBlock(block: Block): Boolean = BlockBreakEvent(block, player).callEvent()

	override fun canPlaceBlock(block: Block, newState: BlockState, placedAgainst: Block) =
		BlockPlaceEvent(block, block.state, placedAgainst, player.activeItem, player, true, EquipmentSlot.HAND).callEvent()

	override fun audience(): Audience = player

	override fun toString(): String {
		return "PlayerController[${player.name}]"
	}
}
