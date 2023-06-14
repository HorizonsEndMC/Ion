package net.horizonsend.ion.server.features.blasters.boundingbox.utils

import org.bukkit.Location
import org.bukkit.entity.Entity

// Credits: QualityArmory
object BoundingBoxUtil {
	fun within2D(e: Entity, closest: Location, minDist: Double, height: Double, centerOffset: Double): Boolean {
		val b1 = within2DHeight(e, closest, height)
		val b2 = within2DWidth(e, closest, minDist, centerOffset)
		return b1 && b2
	}

	fun within2D(
		e: Entity,
		closest: Location,
		minDist: Double,
		height: Double,
		heightOffset: Double,
		centerOffset: Double
	): Boolean {
		val b1 = within2DHeight(e, closest, height, heightOffset)
		val b2 = within2DWidth(e, closest, minDist, centerOffset)
		return b1 && b2
	}

	fun within2DWidth(e: Entity, closest: Location, minDist: Double, centerOffset: Double): Boolean {
		var xS: Double = e.location.clone().add(e.velocity).x - closest.x
		xS *= xS
		var zS: Double = e.location.clone().add(e.velocity).z - closest.z
		zS *= zS
		val distanceSqr = xS + zS
		return distanceSqr <= minDist * minDist
	}

	fun within2DHeight(e: Entity, closest: Location, height: Double): Boolean {
		return within2DHeight(e, closest, height, 0.0)
	}

	fun within2DHeight(e: Entity, closest: Location, height: Double, offset: Double): Boolean {
		val rel: Double = closest.y - e.location.y
		return rel >= offset && rel <= offset + height + e.velocity.y
	}
}
