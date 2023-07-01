package net.horizonsend.ion.server.features.multiblock.starshipweapon.turret

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.minecraft.world.level.block.Rotation
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.util.CARDINAL_BLOCK_FACES
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

	fun getFacing(sign: Sign): BlockFace {
		val block = sign.block

		for (face in CARDINAL_BLOCK_FACES) {
			if (!shape.checkRequirementsSpecific(block, face, loadChunks = true, particles = false)) {
				continue
			}

			return face
		}

		error("Failed to find a face for sign at ${sign.location}")
	}

	fun getFacing(signPos: Vec3i, starship: ActiveStarship): BlockFace {
		val block = signPos.toLocation(starship.serverLevel.world).block
		val sign = block.state as Sign
		return getFacing(sign)
	}

	fun rotate(
		sign: Sign,
		oldFace: BlockFace,
		newFace: BlockFace,
		callback: (sign: Sign, oldKeys: LongOpenHashSet, newKeys: LongOpenHashSet, newFace: BlockFace) -> Unit = { _, _, _, _ -> }
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

		val world = sign.world

		val air = Material.AIR.createBlockData()

		for ((x0, y0, z0) in locations) {
			if (doNotRotate.contains(Vec3i(x0, y0, z0))) continue

			val x = x0 + sign.x
			val y = y0 + sign.y
			val z = z0 + sign.z

			val block = world.getBlockAt(x, y, z)
			val data = block.blockData

			val newData = data.nms.rotate(nmsRotation).createCraftBlockData()

			val nx0 = (x0.toDouble() * cosFactor - z0.toDouble() * sinFactor).roundToInt()
			val nz0 = (x0.toDouble() * sinFactor + z0.toDouble() * cosFactor).roundToInt()

			val nx = nx0 + sign.x
			val nz = nz0 + sign.z

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

		callback(sign, oldKeys, newKeys, newFace)

		return newFace
	}

	private fun placeBlocks(placements: Long2ObjectOpenHashMap<BlockData>, world: World) {
		for ((key, data) in placements) {
			world.getBlockAtKey(key).setBlockData(data, false)
		}
	}
}
