package net.horizonsend.ion.server.features.multiblock

import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.Location
import org.bukkit.World

/**
 * @param x The absolute x position of this multiblock's origin location
 * @param y The absolute x position of this multiblock's origin location
 * @param z The absolute x position of this multiblock's origin location
 *
 * @param world The world this multiblock is in
 *
 * @param type The type of multiblock this entity represents
 **/
abstract class MultiblockEntity(
	val x: Int,
	val y: Int,
	val z: Int,
	val world: World,
	val type: Multiblock
) {
	/**
	 * Returns the origin of this multiblock as a Location
	 **/
	val location get() = Location(world, x.toDouble(), y.toDouble(), z.toDouble())

	/**
	 * Returns the origin of this multiblock as a Vec3i
	 **/
	val vec3i get() = Vec3i(x, y, z)

	/**
	 * Stores any additional data for this multiblock (e.g. power, owner, etc)
	 **/
	abstract fun storeAdditionalData(store: PersistentMultiblockData)

	/**
	 * Returns the serializable data for this multiblock entity
	 *
	 * Additional data is stored via `storeAdditionalData`
	 *
	 * This data is serialized and stored on the chunk when not loaded.
	 **/
	fun store(): PersistentMultiblockData {
		val store = PersistentMultiblockData(x, y, z, type)
		storeAdditionalData(store)

		return store
	}

	/**
	 * Called upon world tick
	 **/
	fun tick() {}

	/**
	 * Called upon world tick.
	 **/
	fun tickAsync() {}
}
