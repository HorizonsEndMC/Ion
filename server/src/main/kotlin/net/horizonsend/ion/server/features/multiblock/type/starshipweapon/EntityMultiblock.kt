package net.horizonsend.ion.server.features.multiblock.type.starshipweapon

import net.horizonsend.ion.server.features.multiblock.ChunkMultiblockManager
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import org.bukkit.World
import org.bukkit.block.BlockFace

/**
 * A multiblock which has a corresponding multiblock entity
 *
 * This interface provides methods for loading and saving the entity data into the chunk
 **/
interface EntityMultiblock<T : MultiblockEntity> {
	/**
	 * Create the multiblock entity using the stored data
	 **/
	fun createEntity(
		manager: ChunkMultiblockManager,
		data: PersistentMultiblockData,
		world: World,
		x: Int,
		y: Int,
		z: Int,
		signOffset: BlockFace
	): T

	fun getMultiblockEntity(world: World, x: Int, y: Int, z: Int): T? {
		val chunkX = x.shr(4)
		val chunkZ = z.shr(4)

		@Suppress("UNCHECKED_CAST")
		return world.ion.getChunk(chunkX, chunkZ)?.multiblockManager?.get(x, y, z) as T?
	}
}
