package net.horizonsend.ion.server.features.multiblock

import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.Location
import org.bukkit.World

/**
 *
 * @param x The absolute x position of this multiblock's origin location
 * @param y The absolute x position of this multiblock's origin location
 * @param z The absolute x position of this multiblock's origin location
 **/
abstract class MultiblockEntity(
	val x: Int,
	val y: Int,
	val z: Int,
	val world: World,
	val multiblock: Multiblock
) {
	val location get() = Location(world, x.toDouble(), y.toDouble(), z.toDouble())

	val vec3i get() = Vec3i(x, y, z)

	abstract fun storeAdditionalData(store: PersistentMultiblockData)

	fun store(): PersistentMultiblockData {
		return PersistentMultiblockData(x, y, z, multiblock)
	}
}
