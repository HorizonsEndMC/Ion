package net.horizonsend.ion.server.features.multiblock.type.starshipweapon

import net.horizonsend.ion.server.features.multiblock.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.PersistentMultiblockData
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import org.bukkit.World

/**
 * A multiblock which has a corresponding multiblock entity
 *
 * This interface provides methods for loading and saving the entity data into the chunk
 **/
interface EntityMultiblock<T : MultiblockEntity> {
	/**
	 * Create the multiblock entity using the stored data
	 **/
	fun createEntity(data: PersistentMultiblockData, world: World, x: Int, y: Int, z: Int): T

	fun getMultiblockEntity(world: World, x: Int, y: Int, z: Int): T? {
		val chunkX = x.shr(4)
		val chunkZ = z.shr(4)

		@Suppress("UNCHECKED_CAST")
		return world.ion.getChunk(chunkX, chunkZ)?.getMultiblockEntity(x, y, z) as T?
	}
}
