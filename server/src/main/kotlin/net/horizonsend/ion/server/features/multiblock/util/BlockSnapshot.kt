package net.horizonsend.ion.server.features.multiblock.util

import org.bukkit.ChunkSnapshot
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData

class BlockSnapshot(val x: Int, val y: Int, val z: Int, val type: Material, val data: BlockData) {
	companion object {
		fun ChunkSnapshot.getBlockSnapshot(x: Int, y: Int, z: Int): BlockSnapshot? {
			if (x shr 4 != getX()) return null
			if (z shr 4 != getZ()) return null

			val localX = x % 16
			val localZ = z % 16

			return BlockSnapshot(x, y, z, getBlockType(localX, y, localZ), getBlockData(localX, y, localZ))
		}

		fun Block.snapshot(): BlockSnapshot = BlockSnapshot(x, y, z, type, blockData)
	}
}
