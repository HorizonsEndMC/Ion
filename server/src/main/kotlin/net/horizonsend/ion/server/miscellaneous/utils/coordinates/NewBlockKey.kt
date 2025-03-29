package net.horizonsend.ion.server.miscellaneous.utils.coordinates

import net.minecraft.core.BlockPos
import org.bukkit.block.BlockFace

/**
 * A world coordinate packed into a 64-bit integer
 *
 * Capable of representing any coordinate in world bounds
 **/
typealias BlockKey = Long

/**
 * Packs a (x, y, z) coordinate into a long
 **/
fun toBlockKey(x: Int, y: Int, z: Int): BlockKey = BlockPos.asLong(x, y, z)

/**
 * Packs a (x, y, z) coordinate into a long
 **/
fun toBlockKey(vec3i: Vec3i): BlockKey = BlockPos.asLong(vec3i.x, vec3i.y, vec3i.z)

/**
 * Creates a Vec3i from a set of packed coordinates
 **/
fun toVec3i(key: BlockKey): Vec3i = Vec3i(getX(key), getY(key), getZ(key))

/**
 * Get the X value of a packed coordinate
 **/
fun getX(key: BlockKey) : Int = BlockPos.getX(key)

/**
 * Get the Y value of a packed coordinate
 **/
fun getY(key: BlockKey) : Int = BlockPos.getY(key)

/**
 * Get the Z value of a packed coordinate
 **/
fun getZ(key: BlockKey) : Int = BlockPos.getZ(key)

fun getRelative(key: BlockKey, direction: BlockFace, distance: Int = 1): BlockKey = toBlockKey(
	getX(key) + (direction.modX * distance),
	getY(key) + (direction.modY * distance),
	getZ(key) + (direction.modZ * distance)
)
