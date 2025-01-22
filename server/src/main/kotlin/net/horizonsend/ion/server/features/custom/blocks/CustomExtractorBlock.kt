package net.horizonsend.ion.server.features.custom.blocks

import net.horizonsend.ion.server.features.custom.items.type.CustomBlockItem
import net.horizonsend.ion.server.features.transport.manager.extractors.data.ExtractorData
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData
import org.bukkit.event.player.PlayerInteractEvent
import java.util.function.Supplier

abstract class CustomExtractorBlock(
	identifier: String,
	blockData: BlockData,
	drops: BlockLoot,
	customBlockItem: Supplier<CustomBlockItem>,
	val guiProvider: (Block, PlayerInteractEvent) -> Unit,

) : InteractableCustomBlock(identifier, blockData, drops, customBlockItem)  {
	abstract fun createExtractorData(pos: BlockKey): ExtractorData

	override fun onRightClick(event: PlayerInteractEvent, block: Block) {
		guiProvider.invoke(block, event)
	}


}
