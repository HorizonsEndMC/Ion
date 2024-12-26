package net.horizonsend.ion.server.features.custom.items.type.tool.mods.tool

import org.bukkit.block.Block
import org.bukkit.block.BlockFace

/**
 * A tool modification that edits a list of blocks, e.g. the blocks mined by the drill
 *
 * An execution priority is necessary
 **/
interface BlockListModifier {
	val priority: Int

	fun modifyBlockList(interactedSide: BlockFace, origin: Block, list: MutableList<Block>)
}
