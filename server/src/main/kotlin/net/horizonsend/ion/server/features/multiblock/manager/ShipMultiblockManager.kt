package net.horizonsend.ion.server.features.multiblock.manager

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.AsyncTickingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.SyncTickingMultiblockEntity
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.horizonsend.ion.server.features.transport.network.TransportNetwork
import net.horizonsend.ion.server.features.transport.node.NetworkType
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getY
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import java.util.concurrent.ConcurrentHashMap

class ShipMultiblockManager(val starship: Starship) : MultiblockManager(IonServer.slF4JLogger) {
	override var multiblockEntities: ConcurrentHashMap<Long, MultiblockEntity> = ConcurrentHashMap()

	/** All the ticked multiblock entities of this chunk */
	override var syncTickingMultiblockEntities: ConcurrentHashMap<Long, SyncTickingMultiblockEntity> = ConcurrentHashMap()
	override var asyncTickingMultiblockEntities: ConcurrentHashMap<Long, AsyncTickingMultiblockEntity> = ConcurrentHashMap()

	override val world get() = starship.world

	override fun save() {}

	override fun getNetwork(type: NetworkType): TransportNetwork {
		return type.get(starship)
	}

	init {
	    loadEntities()
	}

	fun loadEntities() {
		val worldManager = world.ion.multiblockManager

		starship.iterateBlocks { x, y, z ->
			val modernBlockKey = toBlockKey(x, y, z)
			val manager = worldManager.getChunkManager(modernBlockKey) ?: return@iterateBlocks

			manager.getAllMultiblockEntities()[modernBlockKey]?.let {
				multiblockEntities[modernBlockKey] = it
				manager.getAllMultiblockEntities().remove(modernBlockKey)
			} ?: return@iterateBlocks

			manager.syncTickingMultiblockEntities[modernBlockKey]?.let {
				syncTickingMultiblockEntities[modernBlockKey] = it
				manager.syncTickingMultiblockEntities.remove(modernBlockKey)
			}

			manager.asyncTickingMultiblockEntities[modernBlockKey]?.let {
				asyncTickingMultiblockEntities[modernBlockKey] = it
				manager.asyncTickingMultiblockEntities.remove(modernBlockKey)
			}
		}
	}

	fun releaseEntities() {
		val manager = world.ion.multiblockManager

		for ((key, multiblockEntity) in multiblockEntities) {
			val network = manager.getChunkManager(key) ?: continue

			// If it was lost, don't place it back
			if (!multiblockEntity.isIntact(checkSign = true)) {
				multiblockEntity.handleRemoval()
				continue
			}

			network.getAllMultiblockEntities()[key] = multiblockEntity
		}

		for ((key, multiblockEntity) in syncTickingMultiblockEntities) {
			val network = manager.getChunkManager(key) ?: continue

			// If it was lost, don't place it back
			if (!(multiblockEntity as MultiblockEntity).isIntact(checkSign = true)) {
				multiblockEntity.handleRemoval()
				continue
			}

			network.syncTickingMultiblockEntities[key] = multiblockEntity
		}

		for ((key, multiblockEntity) in asyncTickingMultiblockEntities) {
			val network = manager.getChunkManager(key) ?: continue

			// If it was lost, don't place it back
			if (!(multiblockEntity as MultiblockEntity).isIntact(checkSign = true)) {
				multiblockEntity.handleRemoval()
				continue
			}

			network.asyncTickingMultiblockEntities[key] = multiblockEntity
		}
	}

	private fun displaceKey(movement: StarshipMovement, key: BlockKey): BlockKey {
		val x = getX(key)
		val y = getY(key)
		val z = getZ(key)

		return toBlockKey(
			movement.displaceX(x, z),
			movement.displaceY(y),
			movement.displaceZ(z, x),
		)
	}

	fun displaceEntities(movement: StarshipMovement) {
		val newEntities = ConcurrentHashMap<Long, MultiblockEntity>()

		for (entry in multiblockEntities) {
			val entity = entry.value
			entity.displace(movement)

			newEntities[displaceKey(movement, entry.key)] = entity
		}

		multiblockEntities = newEntities

		val newSyncTicking = ConcurrentHashMap<Long, SyncTickingMultiblockEntity>()

		for (entry in syncTickingMultiblockEntities) {
			newSyncTicking[displaceKey(movement, entry.key)] = entry.value
		}

		syncTickingMultiblockEntities = newSyncTicking

		val newAsyncTicking = ConcurrentHashMap<Long, AsyncTickingMultiblockEntity>()

		for (entry in asyncTickingMultiblockEntities) {
			newAsyncTicking[displaceKey(movement, entry.key)] = entry.value
		}

		asyncTickingMultiblockEntities = newAsyncTicking
	}
}
