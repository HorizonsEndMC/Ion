package net.horizonsend.ion.common.utils

open class DBVec3i(val x: Int, val y: Int, val z: Int) {
	operator fun component1() = x
	operator fun component2() = y
	operator fun component3() = z
	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is DBVec3i) return false

		if (x != other.x) return false
		if (y != other.y) return false
		return z == other.z
	}

	override fun hashCode(): Int {
		var result = x
		result = 31 * result + y
		result = 31 * result + z
		return result
	}
}
