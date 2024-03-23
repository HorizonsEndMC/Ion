package net.horizonsend.ion.server.features.multiblock.util

import kotlinx.coroutines.runBlocking
import net.horizonsend.ion.server.features.customblocks.CustomBlock
import net.horizonsend.ion.server.features.customblocks.CustomBlocks
import org.bukkit.Bukkit
import org.bukkit.ChunkSnapshot
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockState
import org.bukkit.block.data.BlockData

class BlockSnapshot(val world: World, val x: Int, val y: Int, val z: Int, val type: Material, val data: BlockData) {
	val state: BlockState? get() {
		return runBlocking { getBukkitBlockState(block, false) }
	}

	val block: Block get() {
		return world.getBlockAt(x, y, z)
	}

	val customBlock: CustomBlock? get() = CustomBlocks.getByBlockData(data)

	companion object {
		fun ChunkSnapshot.getBlockSnapshot(x: Int, y: Int, z: Int): BlockSnapshot? {
			if (x shr 4 != getX()) return null
			if (z shr 4 != getZ()) return null

			val chunkOriginX = getX().shl(4)
			val chunkOriginZ = getZ().shl(4)

			val localX = x - chunkOriginX
			val localZ = z - chunkOriginZ

			return BlockSnapshot(Bukkit.getWorld(worldName)!!, x, y, z, getBlockType(localX, y, localZ), getBlockData(localX, y, localZ))
		}

		fun Block.snapshot(): BlockSnapshot = BlockSnapshot(world, x, y, z, type, blockData)
	}
}
