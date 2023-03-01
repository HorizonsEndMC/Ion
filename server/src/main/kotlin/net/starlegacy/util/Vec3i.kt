package net.starlegacy.util

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.util.Vector

data class Vec3i(val x: Int, val y: Int, val z: Int) {
	@Deprecated("Star Legacy's blockKey is not the same as Minecraft's blockKey")
	constructor(blockKey: Long) : this(blockKeyX(blockKey), blockKeyY(blockKey), blockKeyZ(blockKey))

	constructor(vector: Vector) : this(vector.blockX, vector.blockY, vector.blockZ)

	constructor(location: Location) : this(location.blockX, location.blockY, location.blockZ)

	override fun toString() = "$x,$y,$z"

	fun toLocation(world: World?): Location = Location(world, x.toDouble(), y.toDouble(), z.toDouble())

	@Deprecated("Star Legacy's blockKey is not the same as Minecraft's blockKey")
	fun toBlockKey(): Long = blockKey(x, y, z)

	fun toVector(): Vector = Vector(x, y, z)
	fun toCenterVector(): Vector = Vector(x.toDouble() + 0.5, y.toDouble() + 0.5, z.toDouble() + 0.5)

	fun distance(x: Int, y: Int, z: Int): Double = distance(this.x, this.y, this.z, x, y, z)

	operator fun plus(other: Vec3i) = Vec3i(x + other.x, y + other.y, z + other.z)
	operator fun minus(other: Vec3i) = Vec3i(x - other.x, y - other.y, z - other.z)
}
