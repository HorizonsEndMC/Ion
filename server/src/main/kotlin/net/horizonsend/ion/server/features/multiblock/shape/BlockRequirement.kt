package net.horizonsend.ion.server.features.multiblock.shape

import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData

class BlockRequirement(
	val alias: String,
	var example: (BlockFace) -> BlockData,
	private val syncCheck: (Block, BlockFace, Boolean) -> Boolean,
	private val dataCheck: (BlockData) -> Boolean
) {
	operator fun invoke(block: Block, inward: BlockFace, loadChunks: Boolean) = syncCheck.invoke(block, inward, loadChunks)

	fun checkBlockData(data: BlockData) = dataCheck.invoke(data)

	fun setExample(blockData: BlockData): BlockRequirement {
		this.example = { blockData }

		return this
	}

	fun setExample(type: Material): BlockRequirement {
		this.example = { type.createBlockData() }

		return this
	}
}
