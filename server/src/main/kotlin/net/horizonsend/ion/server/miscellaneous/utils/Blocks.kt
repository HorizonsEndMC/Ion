package net.horizonsend.ion.server.miscellaneous.utils

import com.google.common.collect.ImmutableSet
import net.minecraft.core.BlockPos
import org.bukkit.Axis
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.type.WallSign
import org.bukkit.block.sign.Side
import org.bukkit.block.sign.SignSide
import java.util.EnumSet

/**
 * @see getNMSBlockSateSafe
 */
fun getBlockTypeSafe(world: World, x: Int, y: Int, z: Int): Material? {
	return getNMSBlockSateSafe(world, x, y, z)?.bukkitMaterial
}

/**
 * @see getNMSBlockSateSafe
 */
fun getBlockDataSafe(world: World, x: Int, y: Int, z: Int): BlockData? {
	return getNMSBlockSateSafe(world, x, y, z)?.createCraftBlockData()
}

/**
 * Get block data, with an option to handle chunk loading to get the data
 */
fun getBlockDataSafe(world: World, x: Int, y: Int, z: Int, loadChunks: Boolean = false): BlockData? {
	val data = getBlockDataSafe(world, x, y, z)

	if (data != null) return data
	if (!loadChunks) return null

	return world.getBlockData(x, y, z)
}

/**
 * Gets the block state at the given location if loaded. Sync only.
 */
fun getBlockIfLoaded(world: World, x: Int, y: Int, z: Int): Block? {
	if (y < 0 || y >= world.maxHeight) {
		return null
	}

	return world.minecraft.getChunkIfLoaded(x shr 4, z shr 4)?.cbukkit?.getBlock(x and 15, y, z and 15)
}

/**
 * Given the coordinates of a block, return whether the chunk containing is loaded
 **/
fun isBlockLoaded(world: World, x: Number, y: Number, z: Number): Boolean {
	return world.minecraft.isLoaded(BlockPos(x.toInt(), y.toInt(), z.toInt()))
}

/**
 * Same thing as just getRelative but returns null if it's unloaded.
 */
fun Block.getRelativeIfLoaded(face: BlockFace, distance: Int = 1): Block? {
	val relativeX = face.modX * distance
	val relativeY = face.modY * distance
	val relativeZ = face.modZ * distance
	return getRelativeIfLoaded(relativeX, relativeY, relativeZ)
}

/**
 * Same thing as just getRelative but returns null if it's unloaded.
 */
fun Block.getRelativeIfLoaded(relativeX: Int, relativeY: Int, relativeZ: Int): Block? {
	return getBlockIfLoaded(world, this.x + relativeX, this.y + relativeY, this.z + relativeZ)
}

fun Block.getTypeSafe(): Material? {
	return getBlockTypeSafe(world, x, y, z)
}

/**
 * Gets the block state at the given location if loaded. Sync only.
 * A reference should NOT be held to this.
 */
fun getStateIfLoaded(world: World, x: Int, y: Int, z: Int) = getBlockIfLoaded(world, x, y, z)?.getState(false)
fun getStateSafe(world: World, x: Int, y: Int, z: Int) = getBlockIfLoaded(world, x, y, z)?.getState(true)

val ADJACENT_BLOCK_FACES: Set<BlockFace> = ImmutableSet.of(
	BlockFace.NORTH,
	BlockFace.SOUTH,
	BlockFace.EAST,
	BlockFace.WEST,
	BlockFace.UP,
	BlockFace.DOWN
)

val ADJACENT_PAIRS: Map<Axis, Set<BlockFace>> = mapOf(
	Axis.Z to ImmutableSet.of(BlockFace.NORTH, BlockFace.SOUTH),
	Axis.X to ImmutableSet.of(BlockFace.EAST, BlockFace.WEST),
	Axis.Y to ImmutableSet.of(BlockFace.UP, BlockFace.DOWN)
)

val CARDINAL_BLOCK_FACES: Set<BlockFace> = ImmutableSet.of(
	BlockFace.NORTH,
	BlockFace.SOUTH,
	BlockFace.EAST,
	BlockFace.WEST
)

val ALL_DIRECTIONS: Set<BlockFace> = EnumSet.complementOf(EnumSet.of(BlockFace.SELF))

val BlockFace.rightFace: BlockFace
	get() = when (this) {
		BlockFace.NORTH -> BlockFace.EAST
		BlockFace.EAST -> BlockFace.SOUTH
		BlockFace.SOUTH -> BlockFace.WEST
		BlockFace.WEST -> BlockFace.NORTH
		else -> error("Unsupported direction $this")
	}

val BlockFace.leftFace: BlockFace
	get() = when (this) {
		BlockFace.NORTH -> BlockFace.WEST
		BlockFace.WEST -> BlockFace.SOUTH
		BlockFace.SOUTH -> BlockFace.EAST
		BlockFace.EAST -> BlockFace.NORTH
		else -> error("Unsupported direction $this")
	}

val BlockFace.axis: Axis get() = this.axisOrNull ?: error("Unsupported axis for BlockFace: $this")

val BlockFace.axisOrNull: Axis?
	get() = when (this) {
		BlockFace.NORTH, BlockFace.SOUTH -> Axis.Z
		BlockFace.EAST, BlockFace.WEST -> Axis.X
		BlockFace.UP, BlockFace.DOWN -> Axis.Y
		else -> null
	}

val Axis.faces: Pair<BlockFace, BlockFace>
	get() = when (this) {
		Axis.Z -> BlockFace.NORTH to BlockFace.SOUTH
		Axis.X -> BlockFace.EAST to BlockFace.WEST
		Axis.Y -> BlockFace.UP to BlockFace.DOWN
		else -> error("Unsupported axis for BlockFace: $this")
	}

fun BlockFace.matchesAxis(other: BlockFace) = this.axis == other.axis

fun Sign.getFacing(): BlockFace =
	(this.blockData as? org.bukkit.block.data.type.Sign)?.rotation
		?: (this.blockData as WallSign).facing

fun Sign.front(): SignSide = getSide(Side.FRONT)
fun Sign.back(): SignSide = getSide(Side.BACK)
