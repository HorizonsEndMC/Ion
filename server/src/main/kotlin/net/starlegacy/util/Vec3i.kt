package net.starlegacy.util

import net.minecraft.core.BlockPos
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.util.Vector

data class Vec3i(val x: Int, val y: Int, val z: Int) {
	constructor(blockPos: BlockPos) : this(blockPos.x, blockPos.y, blockPos.z)

	constructor(vector: Vector) : this(vector.blockX, vector.blockY, vector.blockZ)

	constructor(location: Location) : this(location.blockX, location.blockY, location.blockZ)

	override fun toString() = "$x,$y,$z"

	fun toLocation(world: World): Location = Location(world, x.toDouble(), y.toDouble(), z.toDouble())
	fun toVector(): Vector = Vector(x, y, z)
	fun toCenterVector(): Vector = Vector(x.toDouble() + 0.5, y.toDouble() + 0.5, z.toDouble() + 0.5)

	fun toBlockPos(): BlockPos = BlockPos(x, y, z)
	fun distance(x: Int, y: Int, z: Int): Double = distance(this.x, this.y, this.z, x, y, z)

	operator fun plus(other: Vec3i) = Vec3i(x + other.x, y + other.y, z + other.z)
	operator fun minus(other: Vec3i) = Vec3i(x - other.x, y - other.y, z - other.z)
}
