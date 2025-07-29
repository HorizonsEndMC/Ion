package net.horizonsend.ion.server.features.multiblock.manager

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.multiblock.MultiblockAccess
import net.horizonsend.ion.server.features.multiblock.MultiblockEntities
import net.horizonsend.ion.server.features.multiblock.MultiblockTicking
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.linkages.MultiblockLinkageManager
import net.horizonsend.ion.server.features.multiblock.entity.linkages.ShipLinkageManager
import net.horizonsend.ion.server.features.multiblock.entity.type.LegacyMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.AsyncTickingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.SyncTickingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.horizonsend.ion.server.features.transport.manager.TransportManager
import net.horizonsend.ion.server.features.transport.nodes.cache.TransportCache
import net.horizonsend.ion.server.features.transport.nodes.inputs.InputManager
import net.horizonsend.ion.server.features.transport.util.CacheType
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.*
import net.horizonsend.ion.server.miscellaneous.utils.getBlockTypeSafe
import net.horizonsend.ion.server.miscellaneous.utils.getFacing
import net.horizonsend.ion.server.miscellaneous.utils.isWallSign
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import java.util.concurrent.ConcurrentHashMap

class ShipMultiblockManager(val starship: Starship) : MultiblockManager(IonServer.slF4JLogger) {
	var referenceForward: BlockFace = BlockFace.NORTH

	private val multiblockLinkageManager = ShipLinkageManager(this)
	override var multiblockEntities: ConcurrentHashMap<Long, MultiblockEntity> = ConcurrentHashMap()

	/** All the ticked multiblock entities of this chunk */
	override var syncTickingMultiblockEntities: ConcurrentHashMap<Long, SyncTickingMultiblockEntity> = ConcurrentHashMap()
	override var asyncTickingMultiblockEntities: ConcurrentHashMap<Long, AsyncTickingMultiblockEntity> = ConcurrentHashMap()

	override val world get() = starship.world

	override fun getInputManager(): InputManager {
		return starship.transportManager.getInputProvider()
	}

	override fun getTransportManager(): TransportManager<*> = starship.transportManager

	override fun getLinkageManager(): MultiblockLinkageManager {
		return multiblockLinkageManager
	}

	override fun save() {}
	override fun getSignUnsavedTime(): Long = 0
	override fun markChanged() {}

	override fun getNetwork(type: CacheType): TransportCache {
		return type.get(starship)
	}

	fun processLoad(): ShipMultiblockManager {
		loadEntities()
		tryFixEntities()
		multiblockLinkageManager

		MultiblockTicking.registerMultiblockManager(this)

		return this
	}

	private fun loadEntities() {
		val worldManager = world.ion.multiblockManager

		starship.iterateBlocks { x, y, z ->
			val globalKey = toBlockKey(x, y, z)
			val manager = worldManager.getChunkManager(globalKey) ?: return@iterateBlocks

			manager.handleTransferTo(
				globalKey,
				toBlockKey(
					x - starship.centerOfMass.x,
					y - starship.centerOfMass.y,
					z - starship.centerOfMass.z,
				),
				this
			)
		}
	}

	fun onDestroy() {
		MultiblockTicking.removeMultiblockManager(this)
		releaseEntities()
	}

	private fun releaseEntities() {
		val worldManager = world.ion.multiblockManager

		for ((localKey, multiblockEntity) in multiblockEntities) {
			val network = worldManager.getChunkManager(multiblockEntity.globalBlockKey) ?: continue

			// If it was lost, don't place it back
			if (!multiblockEntity.isIntact(checkSign = true)) {
				multiblockEntity.processRemoval()
				continue
			}

			handleTransferTo(
				localKey,
				multiblockEntity.globalBlockKey,
				network
			)
		}
	}

	fun displace(movement: StarshipMovement) {
		for (entry in multiblockEntities) {
			val entity = entry.value
			entity.displace(movement)
		}
	}

	/** Mostly to be used with blueprint load or loadship, loads entities from their sign data */
	private fun tryFixEntities() {
		starship.iterateBlocks { x, y, z ->
			val type = getBlockTypeSafe(world, x, y, z)
			if (type?.isWallSign != true) return@iterateBlocks

			val state = world.getBlockState(x, y, z) as? Sign ?: return@iterateBlocks
			val origin = MultiblockEntity.getOriginFromSign(state)

			if (isOccupied(
					origin.x - starship.centerOfMass.x,
					origin.y - starship.centerOfMass.y,
					origin.z - starship.centerOfMass.z
			)) return@iterateBlocks

			val multiblock = MultiblockAccess.getFast(state)
			if (multiblock !is EntityMultiblock<*>) return@iterateBlocks

			val data = state.persistentDataContainer.get(NamespacedKeys.MULTIBLOCK_ENTITY_DATA, PersistentMultiblockData)
			if (data == null) {
				MultiblockEntities.migrateFromSign(state, multiblock)
				return
			}

			// In case it moved
			data.x = origin.x - starship.centerOfMass.x
			data.y = origin.y - starship.centerOfMass.y
			data.z = origin.z - starship.centerOfMass.z
			data.signOffset = state.getFacing().oppositeFace

			val new = MultiblockEntities.loadFromData(multiblock, this, data)
			if (new is LegacyMultiblockEntity) new.loadFromSign(state)

			addMultiblockEntity(new)
		}
	}

	override fun getGlobalCoordinate(localVec3i: Vec3i): Vec3i {
		return starship.getGlobalCoordinate(localVec3i)
	}

	override fun getLocalCoordinate(globalVec3i: Vec3i): Vec3i {
		return starship.getLocalCoordinate(globalVec3i)
	}

	override fun getGlobalMultiblockEntity(world: World, x: Int, y: Int, z: Int): MultiblockEntity? {
		return get(x, y, z)
	}

	/**
	 * Multiblock entities are stored on the block the sign is placed on.
	 **/
	override operator fun get(sign: Sign): MultiblockEntity? {
		val origin = getRelative(Vec3i(sign.x, sign.y, sign.z), sign.getFacing().oppositeFace, 0, 0, 1)
		val local = getLocalCoordinate(origin)
		return multiblockEntities[toBlockKey(local)]
	}

	fun clearData() {
		multiblockEntities.keys.forEach { key ->
			removeMultiblockEntity(key)
		}
	}

	fun getFromGlobalKey(key: BlockKey): MultiblockEntity? = get(toBlockKey(getLocalCoordinate(toVec3i(key))))
}
