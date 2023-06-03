package net.horizonsend.ion.server.features.blasters.boundingbox

import net.horizonsend.ion.server.features.blasters.boundingbox.utils.BoundingBoxUtil.within2D
import org.bukkit.Location
import org.bukkit.entity.Entity
import kotlin.math.max

class ComplexHumanoidBoundingBox() : AbstractBoundingBox {
	private var bodyWidthRadius = 0.47
	private var headWidthRadius = 0.47
	private var bodyheight = 1.45
	private var headTopHeight = 1.95

	constructor(bodyheight: Double, bodyRadius: Double, headTopHeight: Double, headRadius: Double) : this() {
		this.bodyheight = bodyheight
		bodyWidthRadius = bodyRadius
		this.headTopHeight = headTopHeight
		headWidthRadius = headRadius
	}

	override fun intersects(shooter: Entity, check: Location, base: Entity): Boolean {
		if (intersectsHead(check, base)) return true

		return intersectsBody(check, base)
	}

	override fun allowsHeadshots(): Boolean {
		return true
	}

	override fun intersectsBody(check: Location, base: Entity): Boolean {
		return within2D(base, check, headWidthRadius, bodyheight, max(bodyWidthRadius, headWidthRadius))
	}

	override fun intersectsHead(check: Location, base: Entity): Boolean {
		return within2D(
			base, check, headWidthRadius,
			headTopHeight - bodyheight, bodyheight, max(bodyWidthRadius, headWidthRadius)
		)
	}

	override fun maximumCheckingDistance(base: Entity): Double {
		return max(bodyWidthRadius, headWidthRadius) * 2
	}
}
