package net.horizonsend.ion.server.features.multiblock.util

import net.minecraft.world.level.block.state.BlockState as NMSBlockState
import org.bukkit.block.BlockState as BukkitBlockState
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.future.asDeferred
import net.horizonsend.ion.server.features.multiblock.util.BlockSnapshot.Companion.getBlockSnapshot
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getY
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.loadChunkAsync
import net.horizonsend.ion.server.miscellaneous.utils.getChunkAtIfLoaded
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.chunk.LevelChunk
import org.bukkit.ChunkSnapshot
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData
import org.bukkit.craftbukkit.v1_20_R3.block.CraftBlock
import org.bukkit.craftbukkit.v1_20_R3.block.CraftBlockState
import org.bukkit.craftbukkit.v1_20_R3.block.CraftBlockStates
import org.bukkit.craftbukkit.v1_20_R3.block.data.CraftBlockData
import org.bukkit.craftbukkit.v1_20_R3.util.CraftMagicNumbers
import java.lang.reflect.Method

/**
 * X and Z in real coordinates
 **/
suspend fun getChunkSnapshotAsync(world: World, x: Int, z: Int, loadChunks: Boolean = false): ChunkSnapshot? {
	val chunk = if (loadChunks) {
		world.getChunkAtAsync(x shr 4, z shr 4).asDeferred().await()
	} else {
		world.getChunkAtIfLoaded(x shr 4, z shr 4) ?: return null
	}

	return chunk.chunkSnapshot
}

/** Retrieves a snapshot of an async block */
suspend fun getBlockSnapshotAsync(world: World, x: Int, y: Int, z: Int, loadChunks: Boolean = false): BlockSnapshot? {
	return getChunkSnapshotAsync(world, x, z, loadChunks)?.getBlockSnapshot(x, y, z)
}

/** Retrieves a snapshot of an async block */
suspend fun getBlockSnapshotAsync(world: World, key: Long, loadChunks: Boolean = false): BlockSnapshot? {
	val x = getX(key)
	val y = getY(key)
	val z = getZ(key)
	return getChunkSnapshotAsync(world, x, z, loadChunks)?.getBlockSnapshot(x, y, z)
}

suspend fun <K, V> Map<K, Deferred<V>>.awaitAllValues(): Map<K, V> = if (isEmpty()) mapOf() else mapValues { (_, v) -> v.await() }

suspend fun getNMSTileEntity(block: Block, loadChunks: Boolean): BlockEntity? {
	val serverLevel: ServerLevel = block.world.minecraft
	val blockPos = (block as CraftBlock).position

	if (serverLevel.isOutsideBuildHeight(blockPos)) {
		return null
	}

	val chunkX = block.x.shr(4)
	val chunkZ = block.z.shr(4)

	if (serverLevel.isLoaded(blockPos)) {
		return serverLevel.getChunk(chunkX, chunkZ).getBlockEntity(blockPos, LevelChunk.EntityCreationType.IMMEDIATE)
	}

	if (!loadChunks) {
		return null
	}

	val entity = CompletableDeferred<BlockEntity?>()

	loadChunkAsync(block.world, chunkX, chunkZ) {
		entity.complete(it.minecraft.getBlockEntity(blockPos))
	}

	return entity.await()
}

@Suppress("UNCHECKED_CAST")
suspend fun <T: BlockEntity> getAndCastNMSTileEntity(block: Block, loadChunks: Boolean): T? = getNMSTileEntity(block, loadChunks) as? T

suspend fun getBukkitBlockState(block: Block, loadChunks: Boolean) : BukkitBlockState? {
	val world = block.world
	val blockPos = (block as CraftBlock).position
	val data = getBlockSnapshotAsync(world, blockPos.x, blockPos.y, blockPos.z, loadChunks)?.data ?: return null

	val tileEntity = getNMSTileEntity(block, loadChunks)

	val blockState = createBlockState(world, blockPos, data, tileEntity)
	blockState.worldHandle = world.minecraft

	return blockState
}

val getFactoryForMaterial: Method = CraftBlockStates::class.java.getDeclaredMethod("getFactory", Material::class.java).apply {
	isAccessible = true
}

fun createBlockState(world: World, blockPos: BlockPos, data: BlockData, tileEntity: BlockEntity?): CraftBlockState {
	val material = CraftMagicNumbers.getMaterial((data as CraftBlockData).state.block)

	val factory = getFactoryForMaterial.invoke(null, material)

	val blockStateFactory = factory::class.java.getDeclaredMethod(
		"createBlockState",
		World::class.javaObjectType,
		BlockPos::class.javaObjectType,
		NMSBlockState::class.javaObjectType,
		BlockEntity::class.javaObjectType
	)

	blockStateFactory.isAccessible = true

	return blockStateFactory.invoke(factory, world, blockPos, data.state, tileEntity) as CraftBlockState
}
