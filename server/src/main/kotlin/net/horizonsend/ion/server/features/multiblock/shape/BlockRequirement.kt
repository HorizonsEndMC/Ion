package net.horizonsend.ion.server.features.multiblock.shape

import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.inventory.ItemStack

class BlockRequirement(
	val alias: String,
	var example: (BlockFace) -> BlockData,
	private val syncCheck: (Block, BlockFace, Boolean) -> Boolean,
	private val dataCheck: (BlockData) -> Boolean,
	val itemRequirement: ItemRequirement
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

	class ItemRequirement(
		val itemCheck: (ItemStack) -> Boolean,
		val amountConsumed: (ItemStack) -> Int,
		val toBlock: (ItemStack, BlockFace) -> BlockData
	) {
		fun consume(itemStack: ItemStack): Boolean {
			itemStack.amount -= amountConsumed(itemStack)
			return itemStack.amount >= 0
		}
	}
}
