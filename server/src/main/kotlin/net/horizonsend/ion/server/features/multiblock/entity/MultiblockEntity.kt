package net.horizonsend.ion.server.features.multiblock.entity

import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.MULTIBLOCK_ENTITY_DATA
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.PDCSerializable
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.getFacing
import net.horizonsend.ion.server.miscellaneous.utils.isBlockLoaded
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.persistence.PersistentDataAdapterContext

/**
 * @param manager The multiblock manager that this is registered to
 * @param multiblock The type of multiblock this entity represents
 *
 * @param x The absolute x position of this multiblock's origin location
 * @param y The absolute x position of this multiblock's origin location
 * @param z The absolute x position of this multiblock's origin location
 * @param world The world this multiblock is in
 *
 * @param structureDirection The direction this multiblock is oriented [from the origin]
 **/
abstract class MultiblockEntity(
    var manager: MultiblockManager,
    open val multiblock: Multiblock,

    var x: Int,
    var y: Int,
    var z: Int,
    var world: World,

    var structureDirection: BlockFace
): PDCSerializable<PersistentMultiblockData, PersistentMultiblockData.Companion> {
	private var lastRetrieved = System.currentTimeMillis()

	/** Gets the time since this value was last retrieved */
	protected val deltaTMS: Long get() {
		val time = System.currentTimeMillis()
		val delta = time - lastRetrieved
		lastRetrieved = time

		return delta
	}

	/** Mark this entity as having been removed */
	var removed: Boolean = false

	override val persistentDataType: PersistentMultiblockData.Companion = PersistentMultiblockData.Companion
	val position: BlockKey get() = toBlockKey(x, y, z)

	/**
	 * Returns the origin of this multiblock as a Location
	 **/
	val location get() = Location(world, x.toDouble(), y.toDouble(), z.toDouble())

	/**
	 * Returns the origin of this multiblock as a Vec3i
	 **/
	val vec3i get() = Vec3i(x, y, z)

	val locationKey = toBlockKey(x, y, z)

	/** Logic to be run upon the removal of this entity */
	open fun handleRemoval() {}

	/** Removes this multiblock entity */
	fun remove() {
		manager.removeMultiblockEntity(x, y, z)
	}

	/** Logic to be run upon the unloading of the chunk holding this entity */
	open fun onUnload() {}

	/** Logic to be run upon the loading of the chunk holding this entity, or its creation */
	open fun onLoad() {}

	open fun displaceAdditional(movement: StarshipMovement) {}

	/**
	 * Stores any additional data for this multiblock (e.g. power, owner, etc)
	 **/
	protected open fun storeAdditionalData(store: PersistentMultiblockData, adapterContext: PersistentDataAdapterContext) {}

	/**
	 * Returns the serializable data for this multiblock entity
	 *
	 * Additional data is stored via `storeAdditionalData`
	 *
	 * This data is serialized and stored on the chunk when not loaded.
	 **/
	fun store(): PersistentMultiblockData {
		val store = PersistentMultiblockData(x, y, z, multiblock, structureDirection)
		storeAdditionalData(store, store.getAdditionalDataRaw().adapterContext)

		return store
	}

	fun saveToSign() {
		val sign = getSign() ?: return
		val pdc = sign.persistentDataContainer

		val data = store()
		pdc.set(MULTIBLOCK_ENTITY_DATA, PersistentMultiblockData, data)
		sign.update()
	}

	fun isSignLoaded(): Boolean {
		val signDirection = structureDirection.oppositeFace
		val signLoc = Vec3i(x, y, z) + Vec3i(signDirection.modX, 0, signDirection.modZ)

		return isBlockLoaded(world, signLoc.x, signLoc.y, signLoc.z)
	}

	/**
	 * Gets the sign of this multiblock
	 **/
	fun getSign(): Sign? {
		return getSignFromOrigin(world ,vec3i, structureDirection).state as? Sign
	}

	/**
	 * Gets the origin block of this multiblock
	 **/
	fun getOrigin(): Block {
		return world.getBlockAt(x, y, z)
	}

	/**
	 *
	 **/
	fun isIntact(checkSign: Boolean = true): Boolean {
		if (checkSign && getSign() == null) return false

		return multiblock.blockMatchesStructure(
			world.getBlockAt(x, y, z),
			structureDirection,
			loadChunks = false,
			particles = false
		)
	}

	/**
	 *
	 **/
	fun getBlockRelative(backFourth: Int, leftRight: Int, upDown: Int): Block {
		val (x, y, z) = getRelative(vec3i, structureDirection, backFourth, leftRight, upDown)

		return world.getBlockAt(x, y, z)
	}

	fun getInventory(backFourth: Int, leftRight: Int, upDown: Int): Inventory? {
		return (getBlockRelative(backFourth, leftRight, upDown).getState(false) as? InventoryHolder)?.inventory
	}

	fun displace(movement: StarshipMovement) {
		val newX = movement.displaceX(x, z)
		val newY = movement.displaceY(y)
		val newZ = movement.displaceZ(z, x)

		this.x = newX
		this.y = newY
		this.z = newZ

		val world = movement.newWorld
		if (world != null) {
			this.world = world
		}

		this.structureDirection = movement.displaceFace(structureDirection)

		displaceAdditional(movement)
	}

	companion object {
		/** Get the multiblock's origin from its sign */
		fun getOriginFromSign(sign: Sign): Block {
			val multiblockDirection = sign.getFacing().oppositeFace

			return sign.block.getRelative(multiblockDirection)
		}

		/** Get the sign position from a multiblock's orientation and origin */
		fun getSignFromOrigin(origin: Block, structureDirection: BlockFace): Block = origin.getRelative(structureDirection.oppositeFace)

		/** Get the sign position from a multiblock's orientation and origin */
		fun getSignFromOrigin(world: World, origin: Vec3i, structureDirection: BlockFace): Block {
			val position = origin.getRelative(structureDirection.oppositeFace)
			return world.getBlockAt(position.x, position.y, position.z)
		}
	}

	fun markChanged() {
		manager.markChanged()
	}
}
