package net.horizonsend.ion.server.features.blasters.boundingbox

import net.horizonsend.ion.server.features.blasters.boundingbox.utils.BoundingBoxUtil
import net.horizonsend.ion.server.features.blasters.boundingbox.utils.BoundingBoxUtil.within2DHeight
import org.bukkit.Location
import org.bukkit.entity.Ageable
import org.bukkit.entity.Entity


class DefaultHumanoidBoundingBox() : AbstractBoundingBox {
	private var bodyWidthRadius = 0.48
	private var bodyWidthRadius_baby = 0.48
	private var bodyheight = 1.45
	private var headTopHeight = 1.95
	private var bodyheight_baby = 0.60
	private var headTopHeight_baby = 1.0

	constructor(bodyheight: Double, bodyRadius: Double, headTopHeight: Double) : this() {
		this.bodyheight = bodyheight
		bodyWidthRadius = bodyRadius
		this.headTopHeight = headTopHeight
	}

	constructor(
		bodyheight: Double,
		bodyRadius: Double,
		headTopHeight: Double,
		b_bodyheight: Double,
		b_bodyRadius: Double,
		b_headTopHeight: Double
	) : this(bodyheight, bodyRadius, headTopHeight) {
		bodyheight_baby = b_bodyheight
		bodyWidthRadius_baby = b_bodyRadius
		headTopHeight_baby = b_headTopHeight
	}

	override fun intersects(shooter: Entity, check: Location, base: Entity): Boolean {
		if (base is Ageable && !base.isAdult) {
			val intersectsBody = BoundingBoxUtil.within2DWidth(base, check, bodyWidthRadius_baby, bodyWidthRadius_baby)
			return if (!intersectsBody) false else intersectsHead(check, base) || intersectsBody(check, base)
		}

		val intersectsBody = BoundingBoxUtil.within2DWidth(base, check, bodyWidthRadius, bodyWidthRadius)
		return if (!intersectsBody) false else intersectsHead(check, base) || intersectsBody(check, base)
	}

	override fun allowsHeadshots(): Boolean {
		return true
	}

	override fun intersectsHead(check: Location, base: Entity): Boolean {
		return if (base is Ageable && !base.isAdult) {
			within2DHeight(base, check, headTopHeight_baby - bodyheight_baby, bodyheight_baby)
		} else within2DHeight(base, check, headTopHeight - bodyheight, bodyheight)
	}

	override fun intersectsBody(check: Location, base: Entity): Boolean {
		return if (base is Ageable && !base.isAdult) {
			within2DHeight(base, check, bodyheight_baby)
		} else within2DHeight(base, check, headTopHeight)
	}

	override fun maximumCheckingDistance(base: Entity): Double {
		return bodyWidthRadius * 2
	}
}
