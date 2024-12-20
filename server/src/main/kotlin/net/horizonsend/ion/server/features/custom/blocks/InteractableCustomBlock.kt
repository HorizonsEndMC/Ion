package net.horizonsend.ion.server.features.custom.blocks

import net.horizonsend.ion.server.features.custom.items.type.CustomBlockItem
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData
import org.bukkit.event.player.PlayerInteractEvent
import java.util.function.Supplier

abstract class InteractableCustomBlock(
	identifier: String,
	blockData: BlockData,
	drops: BlockLoot,
	customBlockItem: Supplier<CustomBlockItem>
) : CustomBlock(identifier, blockData, drops, customBlockItem) {
	open fun onRightClick(event: PlayerInteractEvent, block: Block) {}
	open fun onLeftClick(event: PlayerInteractEvent, block: Block) {}
}
