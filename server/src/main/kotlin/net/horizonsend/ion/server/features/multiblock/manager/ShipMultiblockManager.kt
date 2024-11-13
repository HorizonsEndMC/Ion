package net.horizonsend.ion.server.features.multiblock.manager

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.multiblock.MultiblockAccess
import net.horizonsend.ion.server.features.multiblock.MultiblockEntities
import net.horizonsend.ion.server.features.multiblock.MultiblockTicking
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.LegacyMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.AsyncTickingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.SyncTickingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.horizonsend.ion.server.features.transport.nodes.cache.TransportCache
import net.horizonsend.ion.server.features.transport.nodes.inputs.InputManager
import net.horizonsend.ion.server.features.transport.util.CacheType
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getY
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.getBlockTypeSafe
import net.horizonsend.ion.server.miscellaneous.utils.getFacing
import net.horizonsend.ion.server.miscellaneous.utils.isWallSign
import org.bukkit.block.Sign
import java.util.concurrent.ConcurrentHashMap

class ShipMultiblockManager(val starship: Starship) : MultiblockManager(IonServer.slF4JLogger) {
	override var multiblockEntities: ConcurrentHashMap<Long, MultiblockEntity> = ConcurrentHashMap()

	/** All the ticked multiblock entities of this chunk */
	override var syncTickingMultiblockEntities: ConcurrentHashMap<Long, SyncTickingMultiblockEntity> = ConcurrentHashMap()
	override var asyncTickingMultiblockEntities: ConcurrentHashMap<Long, AsyncTickingMultiblockEntity> = ConcurrentHashMap()

	override val world get() = starship.world

	override fun getInputManager(): InputManager {
		return starship.transportManager.getInputProvider()
	}

	override fun save() {}
	override fun getSignUnsavedTime(): Long = 0
	override fun markChanged() {}

	override fun getNetwork(type: CacheType): TransportCache {
		return type.get(starship)
	}

	init {
	    loadEntities()
		MultiblockTicking.registerMultiblockManager(this)
		tryFixEntities()
	}

	fun loadEntities() {
		val worldManager = world.ion.multiblockManager

		starship.iterateBlocks { x, y, z ->
			val modernBlockKey = toBlockKey(x, y, z)
			val manager = worldManager.getChunkManager(modernBlockKey) ?: return@iterateBlocks

			manager.getAllMultiblockEntities()[modernBlockKey]?.let {
				multiblockEntities[modernBlockKey] = it
				it.manager = this
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

	fun release() {
		MultiblockTicking.removeMultiblockManager(this)
		releaseEntities()
	}

	fun releaseEntities() {
		val worldManager = world.ion.multiblockManager

		for ((key, multiblockEntity) in multiblockEntities) {
			val network = worldManager.getChunkManager(key) ?: continue

			// If it was lost, don't place it back
			if (!multiblockEntity.isIntact(checkSign = true)) {
				multiblockEntity.handleRemoval()
				continue
			}

			network.getAllMultiblockEntities()[key] = multiblockEntity
			multiblockEntity.manager = network
		}

		for ((key, multiblockEntity) in syncTickingMultiblockEntities) {
			val network = worldManager.getChunkManager(key) ?: continue

			// If it was lost, don't place it back
			if (!(multiblockEntity as MultiblockEntity).isIntact(checkSign = true)) {
				multiblockEntity.handleRemoval()
				continue
			}

			network.syncTickingMultiblockEntities[key] = multiblockEntity
		}

		for ((key, multiblockEntity) in asyncTickingMultiblockEntities) {
			val network = worldManager.getChunkManager(key) ?: continue

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

	/** Mostly to be used with blueprint load or loadship, loads entities from their sign data */
	private fun tryFixEntities() {
		starship.iterateBlocks { x, y, z ->
			if (isOccupied(x, y, z)) return@iterateBlocks

			val type = getBlockTypeSafe(world, x, y, z)
			if (type?.isWallSign != true) return@iterateBlocks

			val state = world.getBlockState(x, y, z) as? Sign ?: return@iterateBlocks
			val multiblock = MultiblockAccess.getFast(state)
			if (multiblock !is EntityMultiblock<*>) return@iterateBlocks

			val data = state.persistentDataContainer.get(NamespacedKeys.MULTIBLOCK_ENTITY_DATA, PersistentMultiblockData) ?: return MultiblockEntities.migrateFromSign(state, multiblock)

			val origin = MultiblockEntity.getOriginFromSign(state)

			// In case it moved
			data.x = origin.x
			data.y = origin.y
			data.z = origin.z
			data.signOffset = state.getFacing().oppositeFace

			val new = MultiblockEntities.loadFromData(multiblock, this, data)
			if (new is LegacyMultiblockEntity) new.loadFromSign(state)

			addMultiblockEntity(new)
		}
	}
}
