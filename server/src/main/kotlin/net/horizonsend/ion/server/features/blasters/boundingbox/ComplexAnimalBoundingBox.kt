package net.horizonsend.ion.server.features.blasters.boundingbox

import net.horizonsend.ion.server.features.blasters.boundingbox.utils.BoundingBoxUtil.within2D
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.util.Vector
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin


class ComplexAnimalBoundingBox(
	bodyheight: Double,
	bodyRadius: Double,
	headBottomHeight: Double,
	headTopHeight: Double,
	headRadius: Double,
	headOffsetDistance: Double
) :
	AbstractBoundingBox {
	private var bodyWidthRadius = 0.47
	private var headWidthRadius = 0.47
	private var bodyheight = 1.45
	private var headTopHeight = 1.95
	private var headBottomHeight = 1.95
	private var headOffsetDistance = 0.0

	init {
		this.bodyheight = bodyheight
		bodyWidthRadius = bodyRadius
		this.headBottomHeight = headBottomHeight
		this.headTopHeight = headTopHeight
		headWidthRadius = headRadius
		this.headOffsetDistance = headOffsetDistance
	}

	override fun intersects(shooter: Entity, check: Location, base: Entity): Boolean {
		return if (intersectsBody(check, base)) true else intersectsHead(check, base)
	}

	override fun allowsHeadshots(): Boolean {
		return true
	}

	override fun intersectsBody(check: Location, base: Entity): Boolean {
		return within2D(base, check, bodyWidthRadius, bodyheight, bodyWidthRadius)
	}

	override fun intersectsHead(check: Location, base: Entity): Boolean {
		val cos = cos(base.location.yaw / 180 * Math.PI)
		val sin = sin(base.location.yaw / 180 * Math.PI)
		val newVal = Vector(
			headOffsetDistance * cos - 0 * sin, 0.0,
			0 * cos + headOffsetDistance * sin
		)
		val newCheck: Location = check.clone()
		newCheck.subtract(newVal)
		return within2D(
			base, newCheck, headWidthRadius,
			headTopHeight - headBottomHeight, headBottomHeight, max(bodyWidthRadius, headWidthRadius)
		)
	}

	override fun maximumCheckingDistance(base: Entity): Double {
		return max(bodyWidthRadius, headWidthRadius + headOffsetDistance + bodyWidthRadius) * 2
	}
}
