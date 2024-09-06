package net.horizonsend.ion.server.features.multiblock

import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.features.world.chunk.IonChunk
import org.bukkit.World
import org.bukkit.block.Block

object MultiblockEntities {
	/**
	 *
	 **/
	fun getMultiblockEntity(world: World, x: Int, y: Int, z: Int): MultiblockEntity? {
		val ionChunk = getIonChunk(world, x, y, z) ?: return null

		return ionChunk.multiblockManager[x, y, z]
	}

	fun getMultiblockEntity(origin: Block): MultiblockEntity? {
		return getMultiblockEntity(origin.world, origin.x, origin.y, origin.z)
	}

	/**
	 * Add a new multiblock entity to the chunk
	 **/
	fun setMultiblockEntity(world: World, x: Int, y: Int, z: Int, entity: (MultiblockManager) -> MultiblockEntity): Boolean {
		val ionChunk = getIonChunk(world, x, y, z) ?: return false

		ionChunk.region.launch {
			val manager = ionChunk.multiblockManager

			manager.addMultiblockEntity(entity(manager), save = true)
		}

		return true
	}

	/**
	 *
	 **/
	fun removeMultiblockEntity(world: World, x: Int, y: Int, z: Int): MultiblockEntity? {
		val ionChunk = getIonChunk(world, x, y, z) ?: return null

		return ionChunk.multiblockManager.removeMultiblockEntity(x, y, z)
	}

	private fun getIonChunk(world: World, x: Int, y: Int, z: Int): IonChunk? {
		val chunkX = x.shr(4)
		val chunkZ = z.shr(4)

		return world.ion.getChunk(chunkX, chunkZ)
	}
}
