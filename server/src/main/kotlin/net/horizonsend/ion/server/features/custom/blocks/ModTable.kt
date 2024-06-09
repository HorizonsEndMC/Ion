package net.horizonsend.ion.server.features.custom.blocks

import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.server.features.custom.items.CustomItems
import org.bukkit.block.Block
import org.bukkit.block.BlockFace.DOWN
import org.bukkit.block.BlockFace.EAST
import org.bukkit.block.BlockFace.NORTH
import org.bukkit.event.player.PlayerInteractEvent

object ModTable : InteractableCustomBlock(
	identifier = "MOD_TABLE",
	blockData = CustomBlocks.mushroomBlockData(setOf(NORTH, DOWN, EAST)),
	drops = BlockLoot(
		requiredTool = null,
		drops = CustomBlocks.customItemDrop({ CustomItems.MOD_TABLE })
	)) {

	override fun onRightClick(event: PlayerInteractEvent, block: Block) {
		event.player.success("Interactable")
	}
}
