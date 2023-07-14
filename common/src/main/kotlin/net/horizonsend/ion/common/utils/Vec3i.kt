package net.horizonsend.ion.common.utils

open class DBVec3i(val x: Int, val y: Int, val z: Int) {
	operator fun component1() = x
	operator fun component2() = y
	operator fun component3() = z
}
