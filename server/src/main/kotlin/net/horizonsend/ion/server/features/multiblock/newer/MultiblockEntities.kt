package net.horizonsend.ion.server.features.multiblock.newer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.features.world.chunk.IonChunk
import org.bukkit.World

object MultiblockEntities {
	private val multiblockCoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

	fun <T: MultiblockEntity> T.executeAsync(block: suspend () -> Unit) {
		multiblockCoroutineScope.launch { block.invoke() }
	}

	/**
	 *
	 **/
	fun getMultiblockEntity(world: World, x: Int, y: Int, z: Int): MultiblockEntity? {
		val ionChunk = getIonChunk(world, x, y, z) ?: return null

		return ionChunk.multiblockManager[x, y, z]
	}

	/**
	 * Add a new multiblock entity to the chunk
	 **/
	fun setMultiblockEntity(world: World, x: Int, y: Int, z: Int, entity: MultiblockEntity): Boolean {
		val ionChunk = getIonChunk(world, x, y, z) ?: return false

		ionChunk.region.launch {
			ionChunk.multiblockManager.addMultiblockEntity(entity, save = true)
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
