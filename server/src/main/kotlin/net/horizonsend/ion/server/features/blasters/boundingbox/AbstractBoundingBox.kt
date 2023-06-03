package net.horizonsend.ion.server.features.blasters.boundingbox

import org.bukkit.Location
import org.bukkit.entity.Entity

interface AbstractBoundingBox {
	fun intersects(shooter: Entity, check: Location, base: Entity): Boolean
	fun allowsHeadshots(): Boolean
	fun intersectsHead(check: Location, base: Entity): Boolean
	fun intersectsBody(check: Location, base: Entity): Boolean
	fun maximumCheckingDistance(base: Entity): Double
}
