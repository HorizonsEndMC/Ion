package net.starlegacy.util

import net.horizonsend.ion.server.miscellaneous.minecraft
import net.minecraft.core.BlockPos
import net.minecraft.core.BlockPos.MutableBlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.chunk.LevelChunk
import org.bukkit.Chunk
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.craftbukkit.v1_19_R2.CraftChunk
import org.bukkit.craftbukkit.v1_19_R2.block.data.CraftBlockData
import org.bukkit.craftbukkit.v1_19_R2.inventory.CraftItemStack
import net.minecraft.world.item.ItemStack as MinecraftItemStack
import net.minecraft.world.level.block.Block as MinecraftBlock
import org.bukkit.block.Block as BukkitBlock
import org.bukkit.inventory.ItemStack as BukkitItemStack

//region Access Extensions
inline val BlockData.nms: BlockState get() = (this as CraftBlockData).state
inline val Chunk.nms: LevelChunk get() = (this as CraftChunk).handle
inline val LevelChunk.cbukkit: CraftChunk get() = this.bukkitChunk as CraftChunk
//endregion

//region misc
val Direction.blockFace
	get() = when (this) {
		Direction.DOWN -> BlockFace.DOWN
		Direction.UP -> BlockFace.UP
		Direction.NORTH -> BlockFace.NORTH
		Direction.SOUTH -> BlockFace.SOUTH
		Direction.WEST -> BlockFace.WEST
		Direction.EAST -> BlockFace.EAST
	}
//endregion

//region NBT
fun BukkitItemStack.getNBTInt(key: String): Int? {
	val tag = CraftItemStack.asNMSCopy(this).tag ?: return null
	if (!tag.contains(key)) return null
	return tag.getInt(key)
}

fun BukkitItemStack.withNBTString(key: String, value: String): BukkitItemStack {
	val nms: MinecraftItemStack = CraftItemStack.asNMSCopy(this)
	val tag: CompoundTag = nms.tag ?: CompoundTag()
	tag.putString(key, value)
	nms.tag = tag
	return nms.asBukkitCopy().ensureServerConversions()
}

fun BukkitItemStack.getNBTString(key: String): String? {
	val tag = CraftItemStack.asNMSCopy(this).tag ?: return null
	if (!tag.contains(key)) return null
	return tag.getString(key)
}
//endregion

//region Block Data
fun BukkitBlock.getNMSBlockData(): BlockState {
	return world.minecraft.getChunk(x shr 4, z shr 4).getBlockState(x and 15, y, z and 15)
}

/**
 * Will attempt to get the block in a thread safe manner.
 * If the chunk is not loaded or it's outside of the valid Y range, will return null.
 */
fun getNMSBlockDataSafe(world: World, x: Int, y: Int, z: Int): BlockState? {
	if (y < world.minHeight || y > world.maxHeight) {
		return null
	}

	return try {
		val chunk: LevelChunk = world.minecraft.getChunkIfLoaded(x shr 4, z shr 4) ?: return null

		chunk.getBlockState(x and 15, y, z and 15)
	} catch (indexOutOfBounds: IndexOutOfBoundsException) {
		null
	}
}

/**
 * Will attempt to get the block in a thread safe manner.
 * If the chunk is not loaded or it's outside of the valid Y range, will return null.
 */
fun getNMSBlockDataSafe(world: ServerLevel, x: Int, y: Int, z: Int): BlockState? {
	if (y < world.minBuildHeight || y > world.maxBuildHeight) {
		return null
	}

	return try {
		val chunk: LevelChunk = world.getChunkIfLoaded(x shr 4, z shr 4) ?: return null

		chunk.getBlockState(x and 15, y, z and 15)
	} catch (indexOutOfBounds: IndexOutOfBoundsException) {
		null
	}
}

fun MinecraftBlock.isAir(): Boolean = this == Blocks.AIR || this == Blocks.CAVE_AIR || this == Blocks.VOID_AIR

fun World.getChunkAtIfLoaded(chunkX: Int, chunkZ: Int): Chunk? = minecraft.getChunkIfLoaded(chunkX, chunkZ)?.bukkitChunk

fun World.setNMSBlockData(x: Int, y: Int, z: Int, data: BlockState, applyPhysics: Boolean = false): Boolean {
	getBlockAt(x, y, z).setBlockData(data.createCraftBlockData(), applyPhysics)
	return true
}
//endregion

//region Block Positions
fun BlockPos.added(x: Int, y: Int, z: Int): BlockPos {
	return BlockPos(this.x + x, this.y + y, this.z + z)
}

fun MutableBlockPos.add(otherPos: BlockPos) {
	this.x = this.x + otherPos.x
	this.y = this.y + otherPos.y
	this.z = this.z + otherPos.z
}

fun MutableBlockPos.multiply(otherPos: BlockPos) {
	this.x = this.x * otherPos.x
	this.y = this.y * otherPos.y
	this.z = this.z * otherPos.z
}
//endregion
