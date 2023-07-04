package net.horizonsend.ion.server.features.multiblock.starshipweapon.turret

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.minecraft.world.level.block.Rotation
import net.starlegacy.feature.misc.CustomBlocks
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.util.Vec3i
import net.starlegacy.util.blockKey
import net.starlegacy.util.leftFace
import net.starlegacy.util.nms
import net.starlegacy.util.rightFace
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.block.data.BlockData
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

abstract class RotatingMultiblock : Multiblock() {
	open val doNotRotate: Set<Vec3i> = setOf()

	abstract fun getFacing(sign: Sign): BlockFace

	fun getFacing(signPos: Vec3i, starship: ActiveStarship): BlockFace {
		val block = signPos.toLocation(starship.serverLevel.world).block
		val sign = block.state as Sign
		return getFacing(sign)
	}

	fun rotate(
		origin: Vec3i,
		world: World,
		oldFace: BlockFace,
		newFace: BlockFace,
		callback: (sign: Vec3i, oldKeys: LongOpenHashSet, newKeys: LongOpenHashSet, newFace: BlockFace) -> Unit = { _, _, _, _ -> }
	): BlockFace {
		val i = when (newFace) {
			oldFace -> return oldFace
			oldFace.rightFace -> 1
			oldFace.oppositeFace -> 2
			oldFace.leftFace -> 3
			else -> error("Failed to calculate rotation iteration count from $oldFace to $newFace")
		}

		val nmsRotation: Rotation = when (i) {
			1 -> Rotation.CLOCKWISE_90
			2 -> Rotation.CLOCKWISE_180
			3 -> Rotation.COUNTERCLOCKWISE_90
			else -> return oldFace // can only be 0
		}

		val theta: Double = 90.0 * i
		val radians: Double = Math.toRadians(theta)
		val cosFactor: Double = cos(radians)
		val sinFactor: Double = sin(radians)

		val locations = shape.getLocations(oldFace)

		val oldKeys = LongOpenHashSet(locations.size)
		val newKeys = LongOpenHashSet(locations.size)
		val placements = Long2ObjectOpenHashMap<BlockData>()

		val air = Material.AIR.createBlockData()

		for ((x0, y0, z0) in locations) {
			val x = x0 + origin.x
			val y = y0 + origin.y
			val z = z0 + origin.z

			val block = world.getBlockAt(x, y, z)
			val data = block.blockData

			val newData = if (CustomBlocks[data] == null) data.nms.rotate(nmsRotation).createCraftBlockData() else data

			val nx0 = (x0.toDouble() * cosFactor - z0.toDouble() * sinFactor).roundToInt()
			val nz0 = (x0.toDouble() * sinFactor + z0.toDouble() * cosFactor).roundToInt()

			val nx = nx0 + origin.x
			val nz = nz0 + origin.z

			if (!locations.contains(Vec3i(nx0, y0, nz0)) && !world.getBlockAt(nx, y, nz).type.isAir) {
				return oldFace
			}

			val oldKey = blockKey(x, y, z)
			oldKeys.add(oldKey)
			placements.putIfAbsent(oldKey, air) // old block, may have been removed

			val newKey = blockKey(nx, y, nz)
			newKeys.add(newKey)
			placements[newKey] = newData
		}

		placeBlocks(placements, world)

		callback(origin, oldKeys, newKeys, newFace)

		return newFace
	}

	fun <V>Long2ObjectOpenHashMap<V>.putIf(key: Long, value: V, predicate: (Long2ObjectOpenHashMap<V>) -> Boolean) {
		if (predicate(this)) this.put(key, value)
	}

	private fun placeBlocks(placements: Long2ObjectOpenHashMap<BlockData>, world: World) {
		for ((key, data) in placements) {

			world.getBlockAtKey(key).setBlockData(data, false)
		}
	}
}
