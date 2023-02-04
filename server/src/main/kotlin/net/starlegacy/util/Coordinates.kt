package net.starlegacy.util

import net.minecraft.core.BlockPos
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.util.NumberConversions
import org.bukkit.util.Vector
import java.util.function.Consumer
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

val Block.coordinates: Vec3i get() = Vec3i(x, y, z)

fun Location.isInRange(location: Location, radius: Double): Boolean = distanceSquared(location) <= radius.squared()

fun Location.add(x: Int, y: Int, z: Int): Location = add(x.toDouble(), y.toDouble(), z.toDouble())
fun Location.add(trio: Vec3i): Location = add(trio.x.toDouble(), trio.y.toDouble(), trio.z.toDouble())
fun Location.add(face: BlockFace): Location = add(face.modX, face.modY, face.modZ)

fun isValidYLevel(y: Int) = y in 0..Bukkit.getServer().getWorlds()[0].maxHeight

@Deprecated("Star Legacy's blockKey is not the same as Minecraft's blockKey")
fun blockKey(x: Number, y: Number, z: Number): Long =
	x.toLong() and 134217727L or (z.toLong() and 134217727L shl 27) or (y.toLong() shl 54)

// the reason i'm suppressing nothing to inline is that I thing it might have *some* performance benefits to inline the math
@Suppress("NOTHING_TO_INLINE")
@Deprecated("Star Legacy's blockKey is not the same as Minecraft's blockKey")
inline fun blockKeyX(key: Long): Int = (key shl 37 shr 37).toInt()

@Suppress("NOTHING_TO_INLINE")
@Deprecated("Star Legacy's blockKey is not the same as Minecraft's blockKey")
inline fun blockKeyY(key: Long): Int = (key ushr 54).toInt()

@Suppress("NOTHING_TO_INLINE")
@Deprecated("Star Legacy's blockKey is not the same as Minecraft's blockKey")
inline fun blockKeyZ(key: Long): Int = (key shl 10 shr 37).toInt()

fun distanceSquared(fromX: Double, fromY: Double, fromZ: Double, toX: Double, toY: Double, toZ: Double): Double =
	(fromX - toX).squared() + (fromY - toY).squared() + (fromZ - toZ).squared()

fun distance(fromX: Double, fromY: Double, fromZ: Double, toX: Double, toY: Double, toZ: Double): Double =
	sqrt(distanceSquared(fromX, fromY, fromZ, toX, toY, toZ))

fun distance(fromX: Int, fromY: Int, fromZ: Int, toX: Int, toY: Int, toZ: Int): Double =
	sqrt(distanceSquared(fromX.d(), fromY.d(), fromZ.d(), toX.d(), toY.d(), toZ.d()))

fun magnitude(x: Double, y: Double, z: Double): Double = sqrt(x.squared() + y.squared() + z.squared())

fun normalize(x: Double, y: Double, z: Double): Triple<Double, Double, Double> {
	val magnitude = magnitude(x, y, z)
	return when (magnitude) {
		0.0 -> Triple(0.0, 0.0, 0.0)
		else -> Triple(x / magnitude, y / magnitude, z / magnitude)
	}
}

fun chunkKey(chunkX: Int, chunkZ: Int): Long {
	return chunkX.toLong() and 4294967295L or (chunkZ.toLong() and 4294967295L shl 32)
}

fun chunkKeyX(i: Long): Int = (i and 4294967295L).toInt()

fun chunkKeyZ(i: Long): Int = (i.ushr(32) and 4294967295L).toInt()

fun loadChunkAsync(world: World, x: Int, z: Int, callback: (Chunk) -> Unit) {
	world.getChunkAtAsync(x, z, Consumer(callback))
}

fun loadChunkAsync(world: World, location: Location, callback: (Chunk) -> Unit) {
	loadChunkAsync(world, location.blockX shr 4, location.blockZ shr 4, callback)
}

private val cachedSpheres = mutableMapOf<Pair<Int, Double>, List<Vec3i>>()

fun getSphereBlocks(radius: Int, lowerBoundOffset: Double = 0.0): List<Vec3i> =
	cachedSpheres.getOrPut(radius to lowerBoundOffset) {
		val circleBlocks = mutableListOf<Vec3i>()

		val bx = 0
		val by = 0
		val bz = 0

		val lowerBoundSquared = (radius - 1).toDouble().minus(lowerBoundOffset).pow(2)
		val upperBoundSquared = radius.toDouble().pow(2)

		// i have no idea what this does but it works
		for (x in bx - radius..bx + radius) {
			for (y in by - radius..by + radius) {
				for (z in bz - radius..bz + radius) {
					val distance = ((bx - x) * (bx - x) + (bz - z) * (bz - z) + (by - y) * (by - y)).toDouble()
					if (distance >= lowerBoundSquared && distance < upperBoundSquared) {
						circleBlocks.add(Vec3i(x, y, z))
					}
				}
			}
		}

		return@getOrPut circleBlocks
	}

/**
 * Returns a list of equally spaced locations along a vector
 *
 * @param vector: Vector which to locations points on
 * @param points: number of locations
 **/

fun Location.alongVector(vector: Vector, points: Int): List<Location> {
	val locationList = mutableListOf<Location>()

	for (count in 0..points) {
		val progression = this.clone().add(
			vector.clone().multiply(count.toDouble() / points.toDouble())
		)

		locationList.add(progression)
	}

	return locationList
}

private val directionArray = arrayOf(
	BlockFace.EAST,
	BlockFace.WEST,
	BlockFace.SOUTH,
	BlockFace.NORTH,
	BlockFace.UP,
	BlockFace.DOWN
)

fun isInside(location: Location, extraChecks: Int): Boolean {
	fun getRelative(location: Location, direction: BlockFace, i: Int): Block? {
		val x = (direction.modX * i).d()
		val y = (direction.modY * i).d()
		val z = (direction.modZ * i).d()
		val newLocation = location.clone().add(x, y, z)

		return when {
			location.world.isChunkLoaded(newLocation.blockX shr 4, newLocation.blockZ shr 4) -> newLocation.block
			else -> null
		}
	}

	if (location.isChunkLoaded && !location.block.type.isAir) {
		return true
	}

	val airBlocks = HashSet<Block>()

	quickLoop@
	for (direction in directionArray) {
		if (direction.oppositeFace == direction) {
			continue
		}

		var block: Block?

		for (i in 1..189) {
			block = getRelative(location, direction, i)

			if (block == null) {
				continue@quickLoop
			}

			if (block.type != Material.AIR) {
				val relative = getRelative(location, direction, i - 1)

				if (relative != null) {
					airBlocks.add(relative)
				}

				continue@quickLoop
			}
		}
		return false
	}

	var check = 0

	while (check < extraChecks && !airBlocks.isEmpty()) {
		edgeLoop@ for (airBlock in airBlocks.toList()) {
			for (direction in directionArray) {
				if (direction.oppositeFace == direction) {
					continue
				}

				var block: Block?

				for (i in 0..189) {
					block = getRelative(airBlock.location, direction, i)

					if (block == null) {
						break
					}

					if (block.type != Material.AIR) {
						if (i != 0) {
							airBlocks.add(airBlock.getRelative(direction, i))
						}

						airBlocks.remove(airBlock)
						continue@edgeLoop
					}
				}

				return false
			}
		}
		check++
	}

	return true
}

fun BlockPos.toVector() = Vector(this.x, this.y, this.z)

fun BlockPos.toLocation(world: World?) = Location(world, this.x.toDouble(), this.y.toDouble(), this.z.toDouble())

fun Location.toBlockPos() = BlockPos(this.x.roundToInt(), this.y.roundToInt(), this.z.roundToInt())

fun Vector.toBlockPos() = BlockPos(this.x.roundToInt(), this.y.roundToInt(), this.z.roundToInt())

fun getChunkSection(minHeight: Int, maxHeight: Int, y: Int): Int {
	check(y in minHeight..maxHeight)

	return (y - minHeight).shr(4)
}

fun vectorToBlockFace(vector: Vector, includeVertical: Boolean = false): BlockFace {
	val x = vector.x
	val z = vector.z

	if (includeVertical) {
		val x2 = NumberConversions.square(x)
		val z2 = NumberConversions.square(z)
		val xz = sqrt(x2 + z2)
		val pitch = atan(-vector.y / xz)
		val pitchDegrees = Math.floorMod(Math.toDegrees(pitch).roundToInt(), 360)

		if (pitchDegrees in 45..135) {
			return BlockFace.DOWN
		}

		if (pitchDegrees in 225..315) {
			return BlockFace.UP
		}
	}

	val yaw = atan2(-x, z)
	val yawDegrees = Math.floorMod(Math.toDegrees(yaw).roundToInt(), 360)
	return when (yawDegrees) {
		in -45..45 -> BlockFace.SOUTH
		in 45..135 -> BlockFace.WEST
		in 135..225 -> BlockFace.NORTH
		else -> BlockFace.EAST
	}
}
