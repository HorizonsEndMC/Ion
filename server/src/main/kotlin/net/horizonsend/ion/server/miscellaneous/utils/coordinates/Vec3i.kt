package net.horizonsend.ion.server.miscellaneous.utils.coordinates

import com.sk89q.worldedit.math.BlockVector3
import net.horizonsend.ion.common.utils.DBVec3i
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.util.Vector

class Vec3i: DBVec3i {
	constructor(a: DBVec3i) : super(a.x, a.y, a.z)
	constructor(x: Int, y: Int, z: Int) : super(x, y, z)
	@Deprecated("Star Legacy's blockKey is not the same as Minecraft's blockKey")
	constructor(blockKey: Long) : super(blockKeyX(blockKey), blockKeyY(blockKey), blockKeyZ(blockKey))

	constructor(vector: Vector) : super(vector.blockX, vector.blockY, vector.blockZ)

	constructor(location: Location) : super(location.blockX, location.blockY, location.blockZ)

	constructor(vector: BlockVector3) : super(vector.blockX, vector.blockY, vector.blockZ)

	fun toLocation(world: World): Location = Location(world, x.toDouble(), y.toDouble(), z.toDouble())

	@Deprecated("Star Legacy's blockKey is not the same as Minecraft's blockKey")
	fun toBlockKey(): Long = blockKey(x, y, z)

	fun toVector(): Vector = Vector(x, y, z)
	fun toCenterVector(): Vector = Vector(x.toDouble() + 0.5, y.toDouble() + 0.5, z.toDouble() + 0.5)

	fun distance(x: Int, y: Int, z: Int): Double = distance(this.x, this.y, this.z, x, y, z)
	fun distance(other: Vec3i): Double = distance(this.x, this.y, this.z, other.x, other.y, other.z)

	/**
	 * @param other Vector that should be added from this one
	 * @return A new vector with the values added
	 **/
	operator fun plus(other: Vec3i) = Vec3i(x + other.x, y + other.y, z + other.z)

	/**
	 * @param other Vector that should be subtracted from this one
	 * @return A new vector with the values subtracted
	 **/
	operator fun minus(other: Vec3i) = Vec3i(x - other.x, y - other.y, z - other.z)

	fun below(blocks: Int = 1) = Vec3i(x, y - blocks, z)
	operator fun times(m: Double): Vec3i = Vec3i((x * m).toInt(), (y * m).toInt(), (z * m).toInt())
	operator fun times(m: Int): Vec3i = Vec3i((x * m), (y * m), (z * m))
}
