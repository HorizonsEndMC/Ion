package net.horizonsend.ion.server.miscellaneous.utils.coordinates

import net.horizonsend.ion.common.utils.miscellaneous.d
import net.horizonsend.ion.common.utils.miscellaneous.squared
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Rotation
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.util.NumberConversions
import org.bukkit.util.Vector
import org.joml.Vector3f
import java.util.concurrent.ThreadLocalRandom
import java.util.function.Consumer
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

fun Location.isInRange(location: Location, radius: Double): Boolean = distanceSquared(location) <= radius.squared()

fun Location.add(x: Int, y: Int, z: Int): Location = add(x.toDouble(), y.toDouble(), z.toDouble())
fun Location.add(trio: Vec3i): Location = add(trio.x.toDouble(), trio.y.toDouble(), trio.z.toDouble())
fun Location.add(face: BlockFace): Location = add(face.modX, face.modY, face.modZ)

fun BlockPos.toVec3i() = Vec3i(this.x, this.y, this.z)

fun isValidYLevel(y: Int) = y in 0..Bukkit.getServer().worlds[0].maxHeight

/**
 * A set of world coordinates packed into a 64-bit integer
 *
 * Cannot represent negative Y coordinates, deprecated in favor of Minecraft's method
 * @see BlockKey
 **/
@Deprecated("Cannot represent negative Y coordinates, deprecated in favor of Minecraft's method via NewBlockKey")
typealias LegacyBlockKey = Long

@Deprecated("Star Legacy's blockKey is not the same as Minecraft's blockKey")
fun blockKey(x: Number, y: Number, z: Number): LegacyBlockKey =
	x.toLong() and 134217727L or (z.toLong() and 134217727L shl 27) or (y.toLong() shl 54)

// the reason i'm suppressing nothing to inline is that I thing it might have *some* performance benefits to inline the math
@Suppress("NOTHING_TO_INLINE")
@Deprecated("Star Legacy's blockKey is not the same as Minecraft's blockKey")
inline fun blockKeyX(key: LegacyBlockKey): Int = (key shl 37 shr 37).toInt()

@Suppress("NOTHING_TO_INLINE")
@Deprecated("Star Legacy's blockKey is not the same as Minecraft's blockKey")
inline fun blockKeyY(key: LegacyBlockKey): Int = (key ushr 54).toInt()

@Suppress("NOTHING_TO_INLINE")
@Deprecated("Star Legacy's blockKey is not the same as Minecraft's blockKey")
inline fun blockKeyZ(key: LegacyBlockKey): Int = (key shl 10 shr 37).toInt()

fun distanceSquared(fromX: Double, fromY: Double, fromZ: Double, toX: Double, toY: Double, toZ: Double): Double =
	(fromX - toX).squared() + (fromY - toY).squared() + (fromZ - toZ).squared()


fun distanceSquared(from: Vec3i, to: Vec3i): Long {
	val (fromX, fromY, fromZ) = from
	val (toX, toY, toZ) = to

	return distanceSquared(fromX, fromY, fromZ, toX, toY, toZ)
}

fun distanceSquared(fromX: Int, fromY: Int, fromZ: Int, toX: Int, toY: Int, toZ: Int): Long {
	val x: Long = fromX.toLong() - toX.toLong()
	val y: Long = fromY.toLong() - toY.toLong()
	val z: Long = fromZ.toLong() - toZ.toLong()

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
	return when (val magnitude = magnitude(x, y, z)) {
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

fun getPointsBetween(one: Vector, two: Vector, points: Int): List<Vector> {
	val connecting = two.clone().subtract(one)

	val locationList = mutableListOf<Vector>()

	for (count in 0..points) {
		val progression = one.clone().add(connecting.clone().multiply(count.toDouble() / points.toDouble()))

		locationList.add(progression)
	}

	return locationList
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

/**
 * Returns a list of Locations forming a 2D rectangle bounded by two corner Locations
 *
 * @param minLoc: The northwestern corner of the rectangle
 * @param maxLoc: The southeastern corner of the rectangle
 */
fun rectangle(minLoc: Location, maxLoc: Location): List<Location> {
	if (minLoc.world != maxLoc.world) return emptyList()

	val zAxisLength = (maxLoc.z - minLoc.z)
	val xAxisLength = (maxLoc.x - minLoc.x)

	val northVector = Vector(0.0, 0.0, -zAxisLength)
	val eastVector = Vector(xAxisLength, 0.0, 0.0)
	val southVector = Vector(0.0, 0.0, zAxisLength)
	val westVector = Vector(-xAxisLength, 0.0, 0.0)

	val northList = minLoc.alongVector(southVector, zAxisLength.toInt())
	val eastList = maxLoc.alongVector(westVector, xAxisLength.toInt())
	val southList = maxLoc.alongVector(northVector, zAxisLength.toInt())
	val westList = minLoc.alongVector(eastVector, xAxisLength.toInt())

	return (northList + eastList + southList + westList).distinct()
}

fun cube(minLoc: Location, maxLoc: Location): List<Location> {
	val bottom = rectangle(minLoc, Location(maxLoc.world, maxLoc.x, minLoc.y, maxLoc.z))
	val top = rectangle(Location(minLoc.world, minLoc.x, maxLoc.y, minLoc.z), maxLoc)

	val height = maxLoc.y - minLoc.y
	val verticalVector = Vector(0.0, height, 0.0)

	val minList = Location(maxLoc.world, minLoc.x, minLoc.y, minLoc.z).alongVector(verticalVector, height.toInt())
	val minMaxList = Location(maxLoc.world, minLoc.x, minLoc.y, maxLoc.z).alongVector(verticalVector, height.toInt())
	val maxMinList = Location(maxLoc.world, maxLoc.x, minLoc.y, minLoc.z).alongVector(verticalVector, height.toInt())
	val maxList = Location(maxLoc.world, maxLoc.x, minLoc.y, maxLoc.z).alongVector(verticalVector, height.toInt())

	return (top + bottom + minList + minMaxList + maxMinList + maxList).distinct()
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

fun rotateBlockFace(blockFace: BlockFace, rotiation: Rotation): BlockFace {
	return when (rotiation) {
		Rotation.NONE -> blockFace
		Rotation.CLOCKWISE_90 -> when (blockFace) {
			BlockFace.NORTH -> BlockFace.EAST
			BlockFace.EAST -> BlockFace.SOUTH
			BlockFace.SOUTH -> BlockFace.WEST
			BlockFace.WEST -> BlockFace.NORTH
			else -> blockFace
		}
		Rotation.CLOCKWISE_180 -> when (blockFace) {
			BlockFace.NORTH -> BlockFace.SOUTH
			BlockFace.EAST -> BlockFace.WEST
			BlockFace.SOUTH -> BlockFace.NORTH
			BlockFace.WEST -> BlockFace.EAST
			else -> blockFace
		}
		Rotation.COUNTERCLOCKWISE_90 -> when (blockFace) {
			BlockFace.NORTH -> BlockFace.WEST
			BlockFace.WEST -> BlockFace.SOUTH
			BlockFace.SOUTH -> BlockFace.EAST
			BlockFace.EAST -> BlockFace.NORTH
			else -> blockFace
		}
	}
}

fun Location.conePoints(originalVector: Vector, angularOffsetDegrees: Double, points: Int): List<Location> {
	val coordinates = mutableListOf<Location>()
	val angularOffsetRads = Math.toRadians(angularOffsetDegrees)

	for (count in 0..points)  {
		coordinates.add(this.clone().add(originalVector.clone()
			.rotateAroundX(ThreadLocalRandom.current().nextDouble(-angularOffsetRads, angularOffsetRads))
			.rotateAroundY(ThreadLocalRandom.current().nextDouble(-angularOffsetRads, angularOffsetRads))
			.rotateAroundZ(ThreadLocalRandom.current().nextDouble(-angularOffsetRads, angularOffsetRads))
		))
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

	while (check < extraChecks && airBlocks.isNotEmpty()) {
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

fun BlockPos.toLocation(world: World?) = Location(world, this.x.toDouble(), this.y.toDouble(), this.z.toDouble())

fun Location.toBlockPos() = BlockPos(this.x.roundToInt(), this.y.roundToInt(), this.z.roundToInt())

fun Location.toVector3f(): Vector3f = Vector3f(this.x.toFloat(), this.y.toFloat(), this.z.toFloat())

fun Vector.isNan() :Boolean {
	return this.x.isNaN() || this.y.isNaN() || this.z.isNaN()
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

/**
 * Converts yaw to a block face.
 * Note: Uses (0..360) values, if providing yaw in the minecraft format (-180..180), add 180 to the input value.
 **/
fun yawToBlockFace(yawDegrees: Int): BlockFace = when (yawDegrees) {
	in 0..45 -> BlockFace.SOUTH
	in 45..135 -> BlockFace.WEST
	in 135..225 -> BlockFace.NORTH
	in 225..315 -> BlockFace.EAST
	in 315..360 -> BlockFace.SOUTH
	else -> throw IllegalArgumentException("yaw $yawDegrees isn't within 0..360!")
}

fun vectorToPitchYaw(vector: Vector, radians : Boolean= false): Pair<Float, Float> {
	val pitch: Float
	val yaw: Float

	val x = vector.x
	val z = vector.z

	if (x == 0.0 && z == 0.0) {
		if (radians) {
			pitch = (if (vector.y > 0) -Math.PI else Math.PI).toFloat()
			return pitch to 0F
		}
		pitch = if (vector.y > 0) -90F else 90F
		return pitch to 0F
	}

	val theta = atan2(-x, z)

	val x2 = NumberConversions.square(x)
	val z2 = NumberConversions.square(z)
	val xz = sqrt(x2 + z2)

	val phi = atan(-vector.y / xz)

	val twoPi = 2 * Math.PI

	if (radians) {
		yaw = theta.toFloat()
		pitch = phi.toFloat()
	} else {
		yaw = Math.toDegrees((theta + twoPi) % twoPi).toFloat()
		pitch = Math.toDegrees(phi).toFloat()
	}

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
	val x = x; val y = y; val z = z
	val ox = other.x; val oy = other.y; val oz = other.z

	return Vector(+((y * oz) + (z * oy)), -((x * oz) - (z * ox)), +((x * oy) - (y * ox)))
}

/**
 * Linearly interpolates [this] vector with the [other] vector based on a [percentage], where 0.0 is this vector and
 * 1.0 is the [other] vector
 */
fun Vector.lerp(other: Vector, percentage: Double): Vector {
	val coercedPercentage = percentage.coerceIn(0.0, 1.0)
	return this.multiply(1 - coercedPercentage).add(other.clone().multiply(coercedPercentage))
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

	helixAroundVector(origin, direction, radius, limPoints, step, wavelength, offsetRadians) {
		points += it
	}

	return  points
}

fun helixAroundVector(
	origin: Location,
	direction: Vector,
	radius: Double,
	limPoints: Int,
	step: Double = 2 * PI,
	wavelength: Double = 1.0,
	offsetRadians: Double = 0.0,
	consumer: Consumer<Location>
) {
	val theta = (2 * PI)
	val progression = step / theta

	val k = direction.clone().normalize()
	val (i, j) = direction.orthogonalVectors()

	origin.iterateVector(direction, limPoints) { pointAlong, progress ->
		val distance = progress * direction.length() + offsetRadians

		val x = pointAlong.x + (radius * cos(distance * wavelength) * i.x) + (radius * sin(distance * wavelength) * j.x) + (progression * progress * k.x)
		val y = pointAlong.y + (radius * cos(distance * wavelength) * i.y) + (radius * sin(distance * wavelength) * j.y) + (progression * progress * k.y)
		val z = pointAlong.z + (radius * cos(distance * wavelength) * i.z) + (radius * sin(distance * wavelength) * j.z) + (progression * progress * k.z)

		consumer.accept(Location(origin.world, x, y, z))
	}
}



fun Collection<Vector>.average(): Vector {
	if (isEmpty()) return Vector()

	val entries = size.toDouble()

	var sumX = 0.0
	var sumY = 0.0
	var sumZ = 0.0

	for (vector in this) {
		sumX += vector.x
		sumY += vector.y
		sumZ += vector.z
	}

	sumX /= entries
	sumY /= entries
	sumZ /= entries

	return Vector(sumX, sumY, sumZ)
}

/** Returns <x, z> pair */
fun getRadialRandomPoint(minimumDistance: Double, maximumDistance: Double): Pair<Double, Double> {
	// Get a random radian for a polar position
	val radians = ThreadLocalRandom.current().nextDouble(0.0, 2 * PI)

	// Get a point representing the distance from the center
	val distance = ThreadLocalRandom.current().nextDouble(minimumDistance, maximumDistance)

	return cos(radians) * distance to sin(radians) * distance
}

fun Location.getLocationNear(minDistance: Double, maxDistance: Double): Location {
	val (x, z) = getRadialRandomPoint(minDistance, maxDistance)

	val loc = this.clone()

	return loc.add(x, 0.0, z)
}

fun Vec3i.toChunkLocal(): Vec3i = Vec3i(x % 16, y, z % 16)

fun Vec3i.getRelative(direction: BlockFace, distance: Int = 1) = plus(Vec3i(direction.modX * distance, direction.modY * distance, direction.modZ * distance))

fun isAdjacent(first: BlockKey, other: BlockKey): Boolean {
	val xDiff = abs(getX(first) - getX(other))
	val yDiff = abs(getY(first) - getY(other))
	val zDiff = abs(getZ(first) - getZ(other))

	return (xDiff + yDiff + zDiff) == 1
}

fun isAdjacent(first: Vec3i, other: Vec3i): Boolean {
	val xDiff = abs(first.x - other.x)
	val yDiff = abs(first.y - other.y)
	val zDiff = abs(first.z - other.z)

	return (xDiff + yDiff + zDiff) == 1
}

// https://gamedev.stackexchange.com/questions/71397/how-can-i-generate-a-lightning-bolt-effect
fun lightning(startLocation: Location, endLocation: Location, maxGenerations: Int, maxOffset: Double, branchChance: Double): List<Location> {
	var currentOffset = maxOffset

	// Using location pairs to preserve the association between "bends" in the lightning bolt
	val locationPairs = mutableListOf(Pair(startLocation, endLocation))

	// more generations = more smooth
	for (generation in 0 until maxGenerations) {
		// Clone original list so the original is not modified during iteration
		val originalLocationPairs = locationPairs.toList()

		for (locationPair in originalLocationPairs) {
			// Remove the previous pair
			locationPairs.remove(locationPair)

			// Find midpoint of the pair; this will form the "bend in the lightning bolt"
			val midpoint = locationPair.first.toVector().midpoint(locationPair.second.toVector())

			// Offset to create the bend
			midpoint.add(midpoint.clone()
				.crossProduct(Vector(0, 1, 0)) // get a vector that is perpendicular to midpoint
				.normalize().multiply(ThreadLocalRandom.current().nextDouble(-currentOffset, currentOffset)) // normalize and multiply to a random offset length
				.rotateAroundAxis(midpoint, 2 * Math.PI * ThreadLocalRandom.current().nextDouble()) // randomly rotate
			)

			val midLocation = midpoint.toLocation(locationPair.first.world)
			val tailPair = Pair(locationPair.first, midLocation)
			val headPair = Pair(midLocation, locationPair.second)

			locationPairs.add(tailPair)
			locationPairs.add(headPair)

			// Randomly add additional branching
			if (ThreadLocalRandom.current().nextDouble() < branchChance) {
				val newPair = Pair(midLocation, locationPair.second.clone().add(Vector(
					ThreadLocalRandom.current().nextDouble(-currentOffset * 2, currentOffset * 2),
					ThreadLocalRandom.current().nextDouble(-currentOffset * 2, currentOffset * 2),
					ThreadLocalRandom.current().nextDouble(-currentOffset * 2, currentOffset * 2)
				)))
				locationPairs.add(newPair)
			}
		}

		currentOffset /= 2
	}

	val finalLocations = mutableListOf<Location>()
	for (locationPair in locationPairs) {
		finalLocations += locationPair.first.alongVector(locationPair.second.clone().subtract(locationPair.first.clone()).toVector(), 5)
	}

	return finalLocations

	/*
	val originalVector = endLocation.clone().subtract(startLocation).toVector()
	val vectorList = mutableListOf(originalVector)

	for (generation in 0 until maxGenerations) {
		// clone originalVectorList so vectors are not removed during the iteration
		val originalVectorList = vectorList.toList()

		for (vector in originalVectorList) {
			vectorList.remove(vector)

			val midpoint = Vector().midpoint(vector)

			// offset midpoint
			val tailHalf = midpoint.clone().add(midpoint.clone() // new midpoint vector
				.crossProduct(Vector(0, 1, 0)) // get a vector that is perpendicular to midpoint
				.normalize().multiply(ThreadLocalRandom.current().nextDouble(-currentOffset, currentOffset)) // normalize and multiply to a random offset length
				.rotateAroundAxis(midpoint, 2 * Math.PI * ThreadLocalRandom.current().nextDouble()) // randomly rotate
			)
			val headHalf = vector.clone().subtract(tailHalf)

			vectorList.add(tailHalf)
			vectorList.add(headHalf)
		}

		currentOffset /= 2
	}

	val finalLocations = mutableListOf<Location>()
	for (vector in vectorList) {
	}
	return finalLocations
	 */
}
