package net.horizonsend.ion.server.features.custom.blocks

import org.bukkit.block.Block
import org.bukkit.block.data.BlockData
import org.bukkit.event.player.PlayerInteractEvent

abstract class InteractableCustomBlock(identifier: String, blockData: BlockData, drops: BlockLoot) : CustomBlock(identifier, blockData, drops) {
	open fun onRightClick(event: PlayerInteractEvent, block: Block) {}
	open fun onLeftClick(event: PlayerInteractEvent, block: Block) {}
}
