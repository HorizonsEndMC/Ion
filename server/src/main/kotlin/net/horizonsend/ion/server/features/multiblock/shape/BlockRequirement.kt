package net.horizonsend.ion.server.features.multiblock.shape

import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.inventory.ItemStack

class BlockRequirement(
	val alias: String,
	var example: BlockData,
	private val syncCheck: (Block, BlockFace, Boolean) -> Boolean,
	val itemRequirement: ItemRequirement
) {
	var blockUpdate: Boolean = false //TODO impl
	private val placementModifications: MutableList<(BlockFace, BlockData) -> Unit> = mutableListOf()

	operator fun invoke(block: Block, inward: BlockFace, loadChunks: Boolean) = syncCheck.invoke(block, inward, loadChunks)

	fun setExample(blockData: BlockData): BlockRequirement {
		this.example = blockData

		return this
	}

	fun setExample(type: Material): BlockRequirement {
		this.example = type.createBlockData()

		return this
	}

	fun addPlacementModification(modification: (BlockFace, BlockData) -> Unit): BlockRequirement {
		placementModifications.add(modification)

		return this
	}

	fun executePlacementModifications(data: BlockData, face: BlockFace) {
		for (placementModification in placementModifications) {
			placementModification.invoke(face, data)
		}
	}

	fun getExample(face: BlockFace): BlockData {
		return example.clone().apply { executePlacementModifications(this, face) }
	}

	class ItemRequirement(
		val itemCheck: (ItemStack) -> Boolean,
		val amountConsumed: (ItemStack) -> Int,
		val toBlock: (ItemStack) -> BlockData,
		val toItemStack: (BlockData) -> ItemStack
	) {
		fun consume(itemStack: ItemStack): Boolean {
			itemStack.amount -= amountConsumed(itemStack)
			return itemStack.amount >= 0
		}
	}
}
