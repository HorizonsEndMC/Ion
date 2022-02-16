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
typealias CBAdvancement = org.bukkit.craftbukkit.v1_16_R3.advancement.CraftAdvancement

typealias CBPlayer = org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer
typealias CBItemStack = org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack
typealias CBWorld = org.bukkit.craftbukkit.v1_16_R3.CraftWorld
typealias CBChunk = org.bukkit.craftbukkit.v1_16_R3.CraftChunk
typealias CBMagicNumbers = org.bukkit.craftbukkit.v1_16_R3.util.CraftMagicNumbers
typealias CBBlockData = org.bukkit.craftbukkit.v1_16_R3.block.data.CraftBlockData
// nms = net.minecraft.server
typealias NMSItemStack = net.minecraft.server.v1_16_R3.ItemStack

typealias NMSItem = net.minecraft.server.v1_16_R3.Item

typealias NMSNBTTagCompound = net.minecraft.server.v1_16_R3.NBTTagCompound
typealias NMSPlayer = net.minecraft.server.v1_16_R3.EntityPlayer
typealias NMSAdvancement = net.minecraft.server.v1_16_R3.Advancement
typealias NMSMinecraftKey = net.minecraft.server.v1_16_R3.MinecraftKey
typealias NMSWorld = net.minecraft.server.v1_16_R3.World
typealias NMSChunk = net.minecraft.server.v1_16_R3.Chunk
typealias NMSChunkSection = net.minecraft.server.v1_16_R3.ChunkSection
typealias NMSBlock = net.minecraft.server.v1_16_R3.Block
typealias NMSBlocks = net.minecraft.server.v1_16_R3.Blocks
typealias NMSEnumDirection = net.minecraft.server.v1_16_R3.EnumDirection
typealias NMSEnumBlockRotation = net.minecraft.server.v1_16_R3.EnumBlockRotation

typealias NMSBlockData = net.minecraft.server.v1_16_R3.IBlockData

typealias NMSBlockAir = net.minecraft.server.v1_16_R3.BlockAir
typealias NMSBlockTileEntity = net.minecraft.server.v1_16_R3.BlockTileEntity
typealias NMSBlockFurnace = net.minecraft.server.v1_16_R3.BlockFurnace
typealias NMSTileEntity = net.minecraft.server.v1_16_R3.TileEntity

typealias NMSBlockPos = net.minecraft.server.v1_16_R3.BlockPosition
//endregion

//region Access Extensions
inline val Player.cbukkit: CBPlayer get() = this as CBPlayer
inline val Player.nms: NMSPlayer get() = this.cbukkit.handle
inline val BlockData.nms: NMSBlockData get() = (this as CBBlockData).state
inline val NMSPlayer.cbukkit: CBPlayer get() = this.bukkitEntity

inline val Advancement.cbukkit: CBAdvancement get() = this as CBAdvancement
inline val Advancement.nms: NMSAdvancement get() = this.cbukkit.handle
inline val NMSAdvancement.cbukkit: Advancement get() = this.bukkit

inline val World.cbukkit: CBWorld get() = this as CBWorld
inline val World.nms: NMSWorld get() = this.cbukkit.handle
inline val NMSWorld.cbukkit: CBWorld get() = this.world

inline val Chunk.cbukkit: CBChunk get() = this as CBChunk
inline val Chunk.nms: NMSChunk get() = this.cbukkit.handle
inline val NMSChunk.cbukkit: CBChunk get() = this.bukkitChunk as CBChunk
//endregion

//region misc
val NMSEnumDirection.blockFace
	get() = when (this) {
		NMSEnumDirection.DOWN -> BlockFace.DOWN
		NMSEnumDirection.UP -> BlockFace.UP
		NMSEnumDirection.NORTH -> BlockFace.NORTH
		NMSEnumDirection.SOUTH -> BlockFace.SOUTH
		NMSEnumDirection.WEST -> BlockFace.WEST
		NMSEnumDirection.EAST -> BlockFace.EAST
	}
//endregion

//region NBT
fun ItemStack.withNBTInt(key: String, value: Int): ItemStack {
	val nms: NMSItemStack = CBItemStack.asNMSCopy(this)
	val tag: NMSNBTTagCompound = nms.tag ?: NMSNBTTagCompound()
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
	val tag: NMSNBTTagCompound = nms.tag ?: NMSNBTTagCompound()
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
fun Material.toNMSBlockData(): NMSBlockData = createBlockData().nms

val NMSBlockData.bukkitMaterial: Material get() = CBMagicNumbers.getMaterial(this.block)

fun Block.getNMSBlockData(): NMSBlockData {
	return world.nms.getChunkAt(x shr 4, z shr 4).getBlockData(x and 15, y, z and 15)
}

/**
 * Will attempt to get the block in a thread safe manner.
 * If the chunk is not loaded or it's outside of the valid Y range, will return null.
 */
fun getNMSBlockDataSafe(world: World, x: Int, y: Int, z: Int): NMSBlockData? {
	if (y < 0 || y > 255) {
		return null
	}

	return try {
		val chunk: NMSChunk = world.nms.getChunkIfLoaded(x shr 4, z shr 4) ?: return null

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

fun World.setNMSBlockData(x: Int, y: Int, z: Int, data: NMSBlockData, applyPhysics: Boolean = false): Boolean {
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
