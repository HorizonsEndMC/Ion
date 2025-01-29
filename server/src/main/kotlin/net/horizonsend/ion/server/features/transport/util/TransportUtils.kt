package net.horizonsend.ion.server.features.transport.util

import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.entity.BlockEntity
import org.bukkit.World
import org.bukkit.persistence.PersistentDataContainer

fun getBlockEntity(globalBlockKey: BlockKey, world: World): BlockEntity? {
	val globalVec3i = toVec3i(globalBlockKey)
	val nmsChunk = world.minecraft.getChunkIfLoaded(globalVec3i.x.shr(4), globalVec3i.z.shr(4)) ?: return null
	return nmsChunk.getBlockEntity(BlockPos(globalVec3i.x, globalVec3i.y, globalVec3i.z))
}

fun getBlockEntity(globalVec3i: Vec3i, world: World): BlockEntity? {
	val nmsChunk = world.minecraft.getChunkIfLoaded(globalVec3i.x.shr(4), globalVec3i.z.shr(4)) ?: return null
	return nmsChunk.getBlockEntity(BlockPos(globalVec3i.x, globalVec3i.y, globalVec3i.z))
}

fun getPersistentDataContainer(globalBlockKey: BlockKey, world: World): PersistentDataContainer? = getBlockEntity(globalBlockKey, world)?.persistentDataContainer
fun getPersistentDataContainer(globalVec3i: Vec3i, world: World): PersistentDataContainer? = getBlockEntity(globalVec3i, world)?.persistentDataContainer
