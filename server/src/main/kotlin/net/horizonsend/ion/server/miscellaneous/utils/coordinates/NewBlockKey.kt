package net.horizonsend.ion.server.miscellaneous.utils.coordinates

import net.minecraft.core.BlockPos
import org.bukkit.block.BlockFace

/**
 * Packs a x, y, z coordinate into a long
 **/
fun toBlockKey(x: Int, y: Int, z: Int): Long = BlockPos.asLong(x, y, z)
fun toBlockKey(vec3i: Vec3i): Long = BlockPos.asLong(vec3i.x, vec3i.y, vec3i.z)
fun toVec3i(key: Long): Vec3i = Vec3i(getX(key), getY(key), getZ(key))

fun getX(key: Long) : Int = BlockPos.getX(key)
fun getY(key: Long) : Int = BlockPos.getY(key)
fun getZ(key: Long) : Int = BlockPos.getZ(key)

fun getRelative(key: Long, direction: BlockFace, distance: Int = 1): Long = toBlockKey(
	getX(key) + (direction.modX * distance),
	getY(key) + (direction.modY * distance),
	getZ(key) + (direction.modZ * distance)
)
