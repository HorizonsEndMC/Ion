package net.horizonsend.ion.server.features.starship.controllers

import net.horizonsend.ion.server.features.starship.Starship
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import org.bukkit.block.Block
import org.bukkit.block.BlockState
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.EquipmentSlot

abstract class PlayerController(val player: Player, starship: Starship, name: String) : Controller(starship, name) {
	override val pilotName: Component = player.displayName()

	override fun canDestroyBlock(block: Block): Boolean = BlockBreakEvent(block, player).callEvent()

	override fun canPlaceBlock(block: Block, newState: BlockState, placedAgainst: Block): Boolean {
		val event = BlockPlaceEvent(block, block.state, placedAgainst, player.activeItem, player, true, EquipmentSlot.HAND)

		return event.callEvent()
	}

	override fun audience(): Audience = player
}
