package net.horizonsend.ion.server.miscellaneous.utils

import net.minecraft.core.BlockPos

/**
 * Packs a x, y, z coordinate into a long
 **/
fun toBlockKey(x: Int, y: Int, z: Int): Long = BlockPos.asLong(x, y, z)
fun toVec3i(key: Long): Vec3i = Vec3i(BlockPos.getX(key), BlockPos.getY(key), BlockPos.getZ(key))
