package net.horizonsend.ion.server.features.multiblock.shape

import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData

class BlockRequirement(
	val alias: String,
	var example: BlockData,
	private val syncCheck: (Block, BlockFace, Boolean) -> Boolean,
	private val asyncCheck: suspend (Block, BlockFace, Boolean) -> Boolean
) {
	operator fun invoke(block: Block, inward: BlockFace, loadChunks: Boolean) = syncCheck.invoke(block, inward, loadChunks)

	suspend fun checkAsync(block: Block, inward: BlockFace, loadChunks: Boolean) = asyncCheck.invoke(block, inward, loadChunks)

	fun setExample(blockData: BlockData): BlockRequirement {
		this.example = blockData

		return this
	}
}
