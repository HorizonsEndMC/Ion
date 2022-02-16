package net.starlegacy.util

import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.advancement.Advancement
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

//region Aliases

// cb = craftbukkit
typealias CBAdvancement = org.bukkit.craftbukkit.v1_18_R1.advancement.CraftAdvancement

typealias CBPlayer = org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer
typealias CBItemStack = org.bukkit.craftbukkit.v1_18_R1.inventory.CraftItemStack
typealias CBWorld = org.bukkit.craftbukkit.v1_18_R1.CraftWorld
typealias CBChunk = org.bukkit.craftbukkit.v1_18_R1.CraftChunk
typealias CBMagicNumbers = org.bukkit.craftbukkit.v1_18_R1.util.CraftMagicNumbers
typealias CBBlockData = org.bukkit.craftbukkit.v1_18_R1.block.data.CraftBlockData
// nms = net.minecraft.server
typealias NMSItemStack = net.minecraft.world.item.ItemStack

typealias NMSItem = net.minecraft.world.item.Item

typealias NMSCompoundTag = net.minecraft.nbt.CompoundTag
typealias NMSServerPlayer = net.minecraft.server.level.ServerPlayer
typealias NMSAdvancement = net.minecraft.advancements.Advancement
typealias NMSLevel = net.minecraft.world.level.Level
typealias NMSLevelChunk = net.minecraft.world.level.chunk.LevelChunk
typealias NMSBlocks = net.minecraft.world.level.block.Blocks
typealias NMSDirection = net.minecraft.core.Direction

typealias NMSBlockState = net.minecraft.world.level.block.state.BlockState

typealias NMSBaseEntityBlock = net.minecraft.world.level.block.BaseEntityBlock
typealias NMSAbstractFurnaceBlock = net.minecraft.world.level.block.AbstractFurnaceBlock
typealias NMSBlockEntity = net.minecraft.world.level.block.entity.BlockEntity

typealias NMSBlockPos = net.minecraft.core.BlockPos
//endregion

//region Access Extensions
inline val Player.cbukkit: CBPlayer get() = this as CBPlayer
inline val Player.nms: NMSServerPlayer get() = this.cbukkit.handle
inline val BlockData.nms: NMSBlockState get() = (this as CBBlockData).state
inline val NMSServerPlayer.cbukkit: CBPlayer get() = this.bukkitEntity

inline val Advancement.cbukkit: CBAdvancement get() = this as CBAdvancement
inline val Advancement.nms: NMSAdvancement get() = this.cbukkit.handle
inline val NMSAdvancement.cbukkit: Advancement get() = this.bukkit

inline val World.cbukkit: CBWorld get() = this as CBWorld
inline val World.nms: NMSLevel get() = this.cbukkit.handle
inline val NMSLevel.cbukkit: CBWorld get() = this.world

inline val Chunk.cbukkit: CBChunk get() = this as CBChunk
inline val Chunk.nms: NMSLevelChunk get() = this.cbukkit.handle
inline val NMSLevelChunk.cbukkit: CBChunk get() = this.bukkitChunk as CBChunk
//endregion

//region misc
val NMSDirection.blockFace
	get() = when (this) {
		NMSDirection.DOWN -> BlockFace.DOWN
		NMSDirection.UP -> BlockFace.UP
		NMSDirection.NORTH -> BlockFace.NORTH
		NMSDirection.SOUTH -> BlockFace.SOUTH
		NMSDirection.WEST -> BlockFace.WEST
		NMSDirection.EAST -> BlockFace.EAST
	}
//endregion

//region NBT
fun ItemStack.withNBTInt(key: String, value: Int): ItemStack {
	val nms: NMSItemStack = CBItemStack.asNMSCopy(this)
	val tag: NMSCompoundTag = nms.tag ?: NMSCompoundTag()
	tag.setInt(key, value)
	nms.tag = tag
	return nms.asBukkitCopy().ensureServerConversions()
}

fun ItemStack.getNBTInt(key: String): Int? {
	val tag = CBItemStack.asNMSCopy(this).tag ?: return null
	if (!tag.hasKey(key)) return null
	return tag.getInt(key)
}

fun ItemStack.withNBTString(key: String, value: String): ItemStack {
	val nms: NMSItemStack = CBItemStack.asNMSCopy(this)
	val tag: NMSCompoundTag = nms.tag ?: NMSCompoundTag()
	tag.setString(key, value)
	nms.tag = tag
	return nms.asBukkitCopy().ensureServerConversions()
}

fun ItemStack.getNBTString(key: String): String? {
	val tag = CBItemStack.asNMSCopy(this).tag ?: return null
	if (!tag.hasKey(key)) return null
	return tag.getString(key)
}
//endregion

//region Block Data
fun Material.toNMSBlockData(): NMSBlockState = createBlockData().nms

val NMSBlockState.bukkitMaterial: Material get() = CBMagicNumbers.getMaterial(this.block)

fun Block.getNMSBlockData(): NMSBlockState {
	return world.nms.getChunkAt(x shr 4, z shr 4).getBlockData(x and 15, y, z and 15)
}

/**
 * Will attempt to get the block in a thread safe manner.
 * If the chunk is not loaded or it's outside of the valid Y range, will return null.
 */
fun getNMSBlockDataSafe(world: World, x: Int, y: Int, z: Int): NMSBlockState? {
	if (y < 0 || y > 255) {
		return null
	}

	return try {
		val chunk: NMSLevelChunk = world.nms.getChunkIfLoaded(x shr 4, z shr 4) ?: return null

		chunk.getBlockData(x and 15, y, z and 15)
	} catch (indexOutOfBounds: IndexOutOfBoundsException) {
		null
	}
}

private val air = Material.AIR.createBlockData()

fun World.setAir(x: Int, y: Int, z: Int, applyPhysics: Boolean = true) {
	setNMSBlockData(x, y, z, air.nms, applyPhysics)
}

fun World.getChunkAtIfLoaded(chunkX: Int, chunkZ: Int): Chunk? = nms.getChunkIfLoaded(chunkX, chunkZ)?.bukkitChunk

fun World.setNMSBlockData(x: Int, y: Int, z: Int, data: NMSBlockState, applyPhysics: Boolean = false): Boolean {
	getBlockAt(x, y, z).setBlockData(data.createCraftBlockData(), applyPhysics)
	return true
}
//endregion

//region Block Positions
fun NMSBlockPos.added(x: Int, y: Int, z: Int): NMSBlockPos {
	return NMSBlockPos(this.x + x, this.y + y, this.z + z)
}

fun NMSBlockPos.subtracted(x: Int, y: Int, z: Int): NMSBlockPos {
	return NMSBlockPos(this.x - x, this.y - y, this.z - z)
}
//endregion
