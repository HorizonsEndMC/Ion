package net.horizonsend.ion.server.features.multiblock.entity

import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlerHolder
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.type.DisplayMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.linkage.MultiblockLinkageHolder
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.horizonsend.ion.server.features.transport.nodes.inputs.InputsData
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.MULTIBLOCK_ENTITY_DATA
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.PDCSerializable
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.getFacing
import net.horizonsend.ion.server.miscellaneous.utils.getRelativeIfLoaded
import net.horizonsend.ion.server.miscellaneous.utils.isBlockLoaded
import net.horizonsend.ion.server.miscellaneous.utils.rightFace
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
 * @param localOffsetX The local x position of this multiblock's origin location
 * @param localOffsetY The local y position of this multiblock's origin location
 * @param localOffsetZ The local z position of this multiblock's origin location
 * @param world The world this multiblock is in
 *
 * @param structureDirection The direction this multiblock is oriented [from the origin]
 **/
abstract class MultiblockEntity(
	var manager: MultiblockManager,

	open val multiblock: Multiblock,

	var world: World,

	var localOffsetX: Int,
	var localOffsetY: Int,
	var localOffsetZ: Int,

	var structureDirection: BlockFace
): PDCSerializable<PersistentMultiblockData, PersistentMultiblockData.Companion>, DisplayHandlerHolder {
	private var lastRetrieved = System.currentTimeMillis()

	/** Mark this entity as having been removed */
	var removed: Boolean = false
	final override val isAlive: Boolean get() = !removed

	override val persistentDataType: PersistentMultiblockData.Companion = PersistentMultiblockData.Companion

	/**
	 * Returns the location of this multiblock, relative to the global origin, as a Location
	 **/
	val location get() = globalVec3i.toLocation(world)

	/**
	 * Returns the origin of this multiblock as a Vec3i
	 **/
	val localVec3i get() = Vec3i(localOffsetX, localOffsetY, localOffsetZ)
	val globalVec3i get() = manager.getGlobalCoordinate(localVec3i)

	val localBlockKey: BlockKey get() = toBlockKey(localOffsetX, localOffsetY, localOffsetZ)
	val globalBlockKey: BlockKey get() = toBlockKey(globalVec3i)

	/** Gets the time since this value was last retrieved */
	protected val deltaTMS: Long get() {
		val time = System.currentTimeMillis()
		val delta = time - lastRetrieved
		lastRetrieved = time

		return delta
	}

	fun processRemoval() {
		removed = true

		releaseInputs()
		handleRemoval()
		if (this is DisplayMultiblockEntity) displayHandler.remove()
		removeLinkages()
	}

	/** Logic to be run upon the removal of this entity */
	protected open fun handleRemoval() {}

	/** Removes this multiblock entity */
	fun remove() {
		manager.removeMultiblockEntity(localOffsetX, localOffsetY, localOffsetZ)
	}

	/** Logic to be run upon the unloading of the chunk holding this entity */
	open fun onUnload() {}

	fun processLoad() {
		registerInputs()
		if (this is DisplayMultiblockEntity) displayHandler.update()
		onLoad()
	}

	/** Logic to be run upon the loading of the chunk holding this entity, or its creation */
	protected open fun onLoad() {}

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
		val store = PersistentMultiblockData(localOffsetX, localOffsetY, localOffsetZ, multiblock, structureDirection)
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
		val signLoc = globalVec3i + Vec3i(signDirection.modX, 0, signDirection.modZ)

		return isBlockLoaded(world, signLoc.x, signLoc.y, signLoc.z)
	}

	/**
	 * Gets the sign of this multiblock
	 **/
	fun getSign(): Sign? {
		return getSignFromOrigin(world, globalVec3i, structureDirection).state as? Sign
	}

	fun getSignLocation() = getSignFromOrigin(world, globalVec3i, structureDirection).location
	fun getSignBlock() = getSignFromOrigin(world, globalVec3i, structureDirection)
	fun getSignKey() = getRelative(globalBlockKey, structureDirection.oppositeFace)

	/**
	 * Gets the origin block of this multiblock
	 **/
	fun getOrigin(): Block {
		val globalLoc = globalVec3i
		return world.getBlockAt(globalLoc.x, globalLoc.y, globalLoc.z)
	}

	/**
	 *
	 **/
	fun isIntact(checkSign: Boolean = true): Boolean {
		if (checkSign && getSign() == null) return false
		val globalLoc = globalVec3i

		return multiblock.blockMatchesStructure(
			world.getBlockAt(globalLoc.x, globalLoc.y, globalLoc.z),
			structureDirection,
			loadChunks = false,
			particles = false
		)
	}

	fun displace(movement: StarshipMovement) {
		val world = movement.newWorld
		if (world != null) {
			this.world = world
		}

		this.structureDirection = movement.displaceFace(structureDirection)

		displaceAdditional(movement)
		if (this is DisplayMultiblockEntity) this.displayHandler.displace(movement)
	}

	/**
	 *
	 **/
	fun getBlockRelative(right: Int, up: Int, forward: Int): Block {
		val (x, y, z) = getRelative(globalVec3i, structureDirection, right = right, up = up, forward = forward)

		return world.getBlockAt(x, y, z)
	}

	fun getPosRelative(right: Int, up: Int, forward: Int): Vec3i {
		return getRelative(globalVec3i, structureDirection, right = right, up = up, forward = forward)
	}

	fun getKeyRelative(right: Int, up: Int, forward: Int): BlockKey {
		return toBlockKey(getRelative(globalVec3i, structureDirection, right = right, up = up, forward = forward))
	}

	fun getInventory(right: Int, up: Int, forward: Int): Inventory? {
		return (getBlockRelative( right = right, up = up, forward = forward).getState(false) as? InventoryHolder)?.inventory
	}

	fun getSquareRegion(offsetRight: Int, offsetUp: Int, offsetForward: Int, radius: Int, depth: Int, filter: (Block) -> Boolean = { true }): MutableList<Block> {
		val center = getBlockRelative(right = offsetRight, up = offsetUp, forward = offsetForward)
		val right = structureDirection.rightFace

		val blocks = mutableListOf<Block>()

		for (h in -radius .. radius) {
			for (v in -radius .. radius) {
				for (d in 0..depth) {
					val block = center.getRelative(right, h).getRelative(BlockFace.UP, v)
					if (filter(block)) continue
					blocks.add(block)
				}
			}
		}

		return blocks
	}

	fun getRegionWithPoints(
		minOffsetRight: Int,
		minOffsetUp: Int,
		minOffsetForward: Int,
		maxOffsetRight: Int,
		maxOffsetUp: Int,
		maxOffsetForward: Int,
		predicate: (Block) -> Boolean = { true }
	): MutableList<Block> {
		val right = structureDirection.rightFace

		val width = maxOffsetRight - minOffsetRight
		val height = maxOffsetUp - minOffsetUp
		val depth = maxOffsetForward - minOffsetForward

		val origin = getBlockRelative(right = minOffsetRight, up = minOffsetUp, forward = minOffsetForward)

		val blocks = mutableListOf<Block>()

		for (w in 0..width) for (h in 0..height) for (d in 0..depth) {
			val block = origin
				.getRelative(right, w)
				.getRelative(BlockFace.UP, h)
				.getRelative(structureDirection, d)

			if (predicate(block)) blocks.add(block)
		}

		return blocks
	}

	fun getRegionWithDimensions(
		originRightOffset: Int,
		originUpOffset: Int,
		originForwardOffset: Int,
		width: Int,
		height: Int,
		depth: Int,
		predicate: (Block) -> Boolean = { true }
	): MutableList<Block> {
		return getRegionWithPoints(
			originRightOffset,
			originUpOffset,
			originForwardOffset,
			originRightOffset + (width - 1),
			originUpOffset + (height - 1),
			originForwardOffset + (depth - 1),
			predicate
		)
	}

	companion object {
		/** Get the multiblock's origin from its sign */
		fun getOriginFromSign(sign: Sign): Block {
			val multiblockDirection = sign.getFacing().oppositeFace

			return sign.block.getRelative(multiblockDirection)
		}

		/** Get the multiblock's origin from its sign */
		fun getOriginFromSignIfLoaded(sign: Sign): Block? {
			val multiblockDirection = sign.getFacing().oppositeFace

			return sign.block.getRelativeIfLoaded(multiblockDirection)
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

	// Section inputs
	abstract val inputsData: InputsData

	fun registerInputs() {
		inputsData.registerInputs()
	}

	fun releaseInputs() {
		inputsData.releaseInputs()
	}

	// Util
	protected fun none(): InputsData = InputsData.builder(this).build()

	val linkages = mutableListOf<MultiblockLinkageHolder>()

	fun reRegisterLinkages() {
		removeLinkages()
		linkages.forEach { t -> t.register() }
	}

	fun removeLinkages() {
		linkages.forEach { linkage -> linkage.deRegister() }
	}

	override fun handlerGetWorld(): World {
		return world
	}
}
