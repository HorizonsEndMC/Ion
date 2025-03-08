package net.horizonsend.ion.server.features.custom.blocks.misc

import org.bukkit.block.Block
import org.bukkit.event.player.PlayerInteractEvent

interface InteractableCustomBlock {
	fun onRightClick(event: PlayerInteractEvent, block: Block) {}
	fun onLeftClick(event: PlayerInteractEvent, block: Block) {}
}
