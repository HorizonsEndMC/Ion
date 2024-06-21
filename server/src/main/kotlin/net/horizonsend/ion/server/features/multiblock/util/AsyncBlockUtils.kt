package net.horizonsend.ion.server.features.multiblock.util

import net.minecraft.world.level.block.state.BlockState as NMSBlockState
import org.bukkit.block.BlockState as BukkitBlockState
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.future.asDeferred
import net.horizonsend.ion.server.features.multiblock.util.BlockSnapshot.Companion.getBlockSnapshot
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getY
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import net.horizonsend.ion.server.miscellaneous.utils.getBlockDataSafe
import net.horizonsend.ion.server.miscellaneous.utils.getBlockTypeSafe
import net.horizonsend.ion.server.miscellaneous.utils.getChunkAtIfLoaded
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.chunk.LevelChunk
import org.bukkit.Bukkit
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
	val chunkX = x.shr(4)
	val chunkZ = z.shr(4)

	if (world.isChunkLoaded(chunkX, chunkZ)) {
		val type = getBlockTypeSafe(world, x, y, z) ?: return null
		val data = getBlockDataSafe(world, x, y, z) ?: return null

		return BlockSnapshot(world, x, y, z, type, data)
	}

	if (!loadChunks) return null

	val chunk = world.getChunkAtAsync(x, z).asDeferred().await()
	val state = chunk.minecraft.getBlockState(x, y, z)

	return BlockSnapshot(world, x, y, z, state.bukkitMaterial, CraftBlockData.fromData(state))
}

/** Retrieves a snapshot of an async block */
suspend fun getBlockSnapshotAsync(world: World, key: Long, loadChunks: Boolean = false): BlockSnapshot? {
	val x = getX(key)
	val y = getY(key)
	val z = getZ(key)
	return getChunkSnapshotAsync(world, x, z, loadChunks)?.getBlockSnapshot(x, y, z)
}

suspend fun <K, V> Map<K, Deferred<V>>.awaitAllValues(): Map<K, V> = if (isEmpty()) mapOf() else mapValues { (_, v) -> v.await() }

fun getNMSTileEntity(block: Block, loadChunks: Boolean): BlockEntity? {
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

	if (!loadChunks) return null

	val chunk = block.world.getChunkAt(chunkX, chunkZ).minecraft
	return chunk.getBlockEntity(blockPos)
}

@Suppress("UNCHECKED_CAST")
fun <T: BlockEntity> getAndCastNMSTileEntity(block: Block, loadChunks: Boolean): T? = getNMSTileEntity(block, loadChunks) as? T

fun getBukkitBlockState(block: Block, loadChunks: Boolean) : BukkitBlockState? {
	// If this is the main thread, we don't need to do laggy reflection
	if (Bukkit.isPrimaryThread()) {
		return block.state
	}

	val world = block.world
	val blockPos = (block as CraftBlock).position
	val data = getBlockDataSafe(world, blockPos.x, blockPos.y, blockPos.z, loadChunks) ?: return null

	val tileEntity = getNMSTileEntity(block, loadChunks)

	val blockState = createBlockState(world, blockPos, data, tileEntity)
	blockState.worldHandle = world.minecraft

	return blockState
}

val getFactoryForMaterial: Method = CraftBlockStates::class.java.getDeclaredMethod("getFactory", Material::class.java).apply {
	isAccessible = true
}

private val blockStateFactory: LoadingCache<Material, Pair<Any, Method>> = CacheBuilder.newBuilder()
	.weakValues()
	.build(CacheLoader.from { material ->
		val factory = getFactoryForMaterial.invoke(null, material)

		factory to factory::class.java.getDeclaredMethod(
			"createBlockState",
			World::class.javaObjectType,
			BlockPos::class.javaObjectType,
			NMSBlockState::class.javaObjectType,
			BlockEntity::class.javaObjectType
		)
	})

fun createBlockState(world: World, blockPos: BlockPos, data: BlockData, tileEntity: BlockEntity?): CraftBlockState {
	val material = CraftMagicNumbers.getMaterial((data as CraftBlockData).state.block)

	val (factory, blockStateFactory) = blockStateFactory[material]

	blockStateFactory.isAccessible = true

	return blockStateFactory.invoke(factory, world, blockPos, data.state, tileEntity) as CraftBlockState
}
