package net.horizonsend.ion.server.miscellaneous.utils

import kotlin.math.PI
import kotlin.math.sqrt

/**
 * Returns the rotational energy in joules
 **/
fun getRotationalEnergy(angularVelocity: Double, momentOfInteria: Double): Double {
	return 0.5 * momentOfInteria * (angularVelocity * angularVelocity)
}

/**
 * Returns the angular velocity, given rotational energy and moment of inertia
 **/
fun solveForAngularVelocity(rotationalEnergy: Double, momentOfInteria: Double): Double {
	return sqrt((rotationalEnergy * 2) / momentOfInteria)
}

/**
 * Returns the angular velocity, in radians / second
 **/
fun getAngularVelocity(rpm: Double): Double {
	val result = (2.0 * PI) * rpmToHertz(rpm)
	return result
}

