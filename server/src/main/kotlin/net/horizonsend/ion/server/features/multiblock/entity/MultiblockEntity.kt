package net.horizonsend.ion.server.features.multiblock.entity

import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.util.getBukkitBlockState
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign

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
	val type: Multiblock,

	var x: Int,
	var y: Int,
	var z: Int,
	var world: World,
	var signOffset: BlockFace
) {
	/**
	 * Returns the origin of this multiblock as a Location
	 **/
	val location get() = Location(world, x.toDouble(), y.toDouble(), z.toDouble())

	/**
	 * Returns the origin of this multiblock as a Vec3i
	 **/
	val vec3i get() = Vec3i(x, y, z)

	val locationKey = toBlockKey(x, y, z)

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
		val store = PersistentMultiblockData(x, y, z, type, signOffset)
		storeAdditionalData(store)

		return store
	}

	/**
	 * Gets the sign of this multiblock
	 **/
	suspend fun getSign(): Sign? {
		val signLoc = Vec3i(x, y, z) + Vec3i(signOffset.modX, 0, signOffset.modZ)

		return getBukkitBlockState(world.getBlockAt(signLoc.x, signLoc.y, signLoc.z), loadChunks = false) as? Sign
	}

	suspend fun isIntact(): Boolean {
		val sign = getSign() ?: return false

		return type.signMatchesStructureAsync(sign)
	}

	/**
	 *
	 **/
	fun getBlockRelative(backFourth: Int, leftRight: Int, upDown: Int): Block {
		val (x, y, z) = getRelative(vec3i, signOffset.oppositeFace, backFourth, leftRight, upDown)

		return world.getBlockAt(x, y, z)
	}
}
