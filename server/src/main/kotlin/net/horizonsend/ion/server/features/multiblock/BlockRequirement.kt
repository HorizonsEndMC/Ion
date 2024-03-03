package net.horizonsend.ion.server.features.multiblock

import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData

class BlockRequirement(
	val example: BlockData,
	private val check: (Block, BlockFace, Boolean) -> Boolean,
	private val asyncCheck: suspend (Block, BlockFace, Boolean) -> Boolean
) {
	operator fun invoke(block: Block, inward: BlockFace, loadChunks: Boolean) = check.invoke(block, inward, loadChunks)

	suspend fun checkAsync(block: Block, inward: BlockFace, loadChunks: Boolean) = asyncCheck.invoke(block, inward, loadChunks)
}
