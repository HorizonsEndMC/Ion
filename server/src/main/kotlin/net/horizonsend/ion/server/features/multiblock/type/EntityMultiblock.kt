package net.horizonsend.ion.server.features.multiblock.type

import net.horizonsend.ion.server.features.multiblock.MultiblockEntities
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign

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
		manager: MultiblockManager,
		data: PersistentMultiblockData,
		world: World,
		x: Int,
		y: Int,
		z: Int,
		structureDirection: BlockFace
	): T

	fun getMultiblockEntity(world: World, x: Int, y: Int, z: Int): T? {
		val chunkX = x.shr(4)
		val chunkZ = z.shr(4)

		@Suppress("UNCHECKED_CAST")
		return world.ion.getChunk(chunkX, chunkZ)?.multiblockManager?.get(x, y, z) as T?
	}

	fun getMultiblockEntity(sign: Sign): T? {
		val origin = MultiblockEntity.getOriginFromSign(sign)

		val world = sign.world
		val ship = ActiveStarships.getInWorld(world).firstOrNull { it.contains(origin.x, origin.y, origin.z) }

		if (ship != null) {
			@Suppress("UNCHECKED_CAST")
			return ship.multiblockManager[origin.x, origin.y, origin.z] as T?
		}

		@Suppress("UNCHECKED_CAST")
		return MultiblockEntities.getMultiblockEntity(origin) as T?
	}
}
