package net.horizonsend.ion.server.miscellaneous.utils

import net.horizonsend.ion.common.utils.miscellaneous.d
import net.horizonsend.ion.common.utils.miscellaneous.squared
import net.minecraft.core.BlockPos
import net.minecraft.world.level.ChunkPos
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
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

val Block.coordinates: Vec3i get() = Vec3i(x, y, z)

fun Location.isInRange(location: Location, radius: Double): Boolean = distanceSquared(location) <= radius.squared()

fun Location.add(x: Int, y: Int, z: Int): Location = add(x.toDouble(), y.toDouble(), z.toDouble())
fun Location.add(trio: Vec3i): Location = add(trio.x.toDouble(), trio.y.toDouble(), trio.z.toDouble())
fun Location.add(face: BlockFace): Location = add(face.modX, face.modY, face.modZ)

operator fun Location.component1(): World? = this.world
operator fun Location.component2(): Double = this.x
operator fun Location.component3(): Double = this.y
operator fun Location.component4(): Double = this.z

fun BlockPos.toVec3i() = Vec3i(this.x, this.y, this.z)

fun isValidYLevel(y: Int) = y in 0..Bukkit.getServer().worlds[0].maxHeight

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


fun distanceSquared(from: Vec3i, to: Vec3i): Int {
	val (fromX, fromY, fromZ) = from
	val (toX, toY, toZ) = to

	return distanceSquared(fromX, fromY, fromZ, toX, toY, toZ)
}
fun distanceSquared(fromX: Int, fromY: Int, fromZ: Int, toX: Int, toY: Int, toZ: Int): Int {
	val x = fromX - toX
	val y = fromY - toY
	val z = fromZ - toZ

	return (x * x) + (y * y) + (z * z)
}

fun distance(from: Vector, to: Vector): Double {
	val fromX = from.x
	val fromY = from.y
	val fromZ = from.z
	val toX = to.x
	val toY = to.y
	val toZ = to.z

	return distance(fromX, fromY, fromZ, toX, toY, toZ)
}

fun distanceSquared(from: Vector, to: Vector): Double {
	val fromX = from.x
	val fromY = from.y
	val fromZ = from.z
	val toX = to.x
	val toY = to.y
	val toZ = to.z

	return distanceSquared(fromX, fromY, fromZ, toX, toY, toZ)
}

fun distance(fromX: Double, fromY: Double, fromZ: Double, toX: Double, toY: Double, toZ: Double): Double =
	sqrt(distanceSquared(fromX, fromY, fromZ, toX, toY, toZ))

fun distance(fromX: Int, fromY: Int, fromZ: Int, toX: Int, toY: Int, toZ: Int): Double =
	sqrt(distanceSquared(fromX.d(), fromY.d(), fromZ.d(), toX.d(), toY.d(), toZ.d()))

fun distance(fromX: Int, fromZ: Int, toX: Int, toZ: Int): Double =
	sqrt(((fromX - toX).squared() + (fromZ - toZ).squared()).d())

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

fun Location.iterateVector(vector: Vector, points: Int, function: (Location, Double) -> Unit) {
	for (count in 0..points) {
		val progress = count.toDouble() / points.toDouble()
		val progression = this.clone().add(vector.clone().multiply(progress))

		function(progression, progress)
	}
}

fun Location.spherePoints(radius: Double, points: Int): List<Location> {
	val goldenRatio = (1.0 + 5.0.pow(0.5)) / 2.0
	val coordinates = mutableListOf<Location>()

	for (count in 0..points) {
		val theta = 2 * PI * count / goldenRatio
		val phi = acos(1.0 - ((2.0 * (count + 0.5)) / points) )

		val x = cos(theta) * sin(phi) * radius
		val y = sin(theta) * sin(phi) * radius
		val z = cos(phi) * radius

		coordinates.add(Location(this.world, x, y, z).add(this))
	}

	return coordinates
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

operator fun BlockPos.component1(): Int = this.x

operator fun BlockPos.component2(): Int = this.y

operator fun BlockPos.component3(): Int = this.z

operator fun BlockPos.minus(other: BlockPos) = BlockPos(this.x - other.x, this.y - other.y, this.z - other.z)

operator fun Triple<Int, Int, Int>.minus(other: Triple<Int, Int, Int>) = Triple(this.first - other.first, this.second - other.second, this.third - other.third)

operator fun ChunkPos.component1(): Int = this.x

operator fun ChunkPos.component2(): Int = this.z

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

//	val yaw = atan2(-x, z)
//	val yawDegrees = Math.floorMod(Math.toDegrees(yaw).roundToInt(), 360)

	val twoPi = 2 * Math.PI
	val theta = atan2(-x, z)
	val yawDegrees = Math.toDegrees((theta + twoPi) % twoPi).toInt()

	return yawToBlockFace(yawDegrees)
}

fun yawToBlockFace(yawDegrees: Int): BlockFace = when (yawDegrees) {
	in 0..45 -> BlockFace.SOUTH
	in 45..135 -> BlockFace.WEST
	in 135..225 -> BlockFace.NORTH
	in 225..315 -> BlockFace.EAST
	in 315..360 -> BlockFace.SOUTH
	else -> throw IllegalArgumentException()
}

fun vectorToPitchYaw(vector: Vector): Pair<Float, Float> {
	val pitch: Float
	val yaw: Float

	val twoPi = 2 * Math.PI
	val x = vector.x
	val z = vector.z

	if (x == 0.0 && z == 0.0) {
		pitch = if (vector.y > 0) -90F else 90F
		return pitch to 0F
	}

	val theta = atan2(-x, z)
	yaw = Math.toDegrees((theta + twoPi) % twoPi).toFloat()

	val x2 = NumberConversions.square(x)
	val z2 = NumberConversions.square(z)
	val xz = sqrt(x2 + z2)
	pitch = Math.toDegrees(atan(-vector.y / xz)).toFloat()

	return pitch to yaw
}

fun getDirection(origin: Vec3i, destination: Vec3i): Vector = destination.minus(origin).toVector()

/** Find the closest point along the vector to the vector **/
fun nearestPointToVector(origin: Vector, direction: Vector, point: Vector): Vector {
	val endPoint = origin.clone().add(direction)

	val v = origin.clone().subtract(endPoint)
	val u = endPoint.clone().subtract(point)

	// The distance between the end and the origin as a fraction of the length of the line
	val distance = -(v.clone().dot(u) / v.clone().dot(v))

	return endPoint.clone().subtract(direction.clone().multiply(distance))
}

/** Find the distance to closest point along the vector to the location **/
fun distanceToVector(origin: Vector, direction: Vector, point: Vector): Double {
	val closestPoint = nearestPointToVector(origin, direction, point)

	return closestPoint.distance(point)
}

fun cartesianProduct(a: Set<*>, b: Set<*>, vararg sets: Set<*>): Set<List<*>> =
	(setOf(a, b).plus(sets))
		.fold(listOf(listOf<Any?>())) { acc, set ->
			acc.flatMap { list -> set.map { element -> list + element } }
		}
		.toSet()

fun Vector.orthogonalVectors(): Pair<Vector, Vector> {
	val right = this.getCrossProduct(BlockFace.UP.direction)
	val next = this.orthogonalThird(right)

	return right.normalize() to next.normalize()
}

fun Vector.orthogonalThird(other: Vector): Vector {
	val x = x; val y = y; val z = z;
	val ox = other.x; val oy = other.y; val oz = other.z;

	return Vector(+((y * oz) + (z * oy)), -((x * oz) - (z * ox)), +((x * oy) - (y * ox)))
}

fun helixAroundVector(
	origin: Location,
	direction: Vector,
	radius: Double,
	limPoints: Int,
	step: Double = 2 * PI,
	wavelength: Double = 1.0,
	offsetRadians: Double = 0.0
): List<Location> {
	val points = mutableListOf<Location>()
	val theta = (2 * PI)
	val progression = step / theta

	val k = direction.clone().normalize()
	val (i, j) = direction.orthogonalVectors()

	origin.iterateVector(direction, limPoints) { pointAlong, progress ->
		val distance = progress * direction.length() + offsetRadians

		val x = pointAlong.x + (radius * cos(distance * wavelength) * i.x) + (radius * sin(distance * wavelength) * j.x) + (progression * progress * k.x)
		val y = pointAlong.y + (radius * cos(distance * wavelength) * i.y) + (radius * sin(distance * wavelength) * j.y) + (progression * progress * k.y)
		val z = pointAlong.z + (radius * cos(distance * wavelength) * i.z) + (radius * sin(distance * wavelength) * j.z) + (progression * progress * k.z)

		points += Location(origin.world, x, y, z)
	}

	return  points
}
