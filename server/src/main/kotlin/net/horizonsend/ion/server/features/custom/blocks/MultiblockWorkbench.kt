package net.horizonsend.ion.server.features.custom.blocks

import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks.customItemDrop
import net.horizonsend.ion.server.features.custom.items.CustomItems
import net.horizonsend.ion.server.miscellaneous.utils.PerPlayerCooldown
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import java.util.concurrent.TimeUnit

object MultiblockWorkbench : InteractableCustomBlock(
	identifier = "MULTIBLOCK_WORKBENCH",
	blockData = CustomBlocks.mushroomBlockData(setOf(BlockFace.NORTH, BlockFace.DOWN, BlockFace.EAST)),
	drops = BlockLoot(
		requiredTool = null,
		drops = customItemDrop(CustomItems::MULTIBLOCK_WORKBENCH, 1)
	)
) {
	private val cooldown = PerPlayerCooldown(5L, TimeUnit.MILLISECONDS)

	override fun onRightClick(event: PlayerInteractEvent, block: Block) {
		val player = event.player
		cooldown.tryExec(player) { openMenu(player) }
	}

	private fun openMenu(player: Player) {

	}


}
