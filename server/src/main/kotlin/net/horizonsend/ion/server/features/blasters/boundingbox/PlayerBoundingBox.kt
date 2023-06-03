package net.horizonsend.ion.server.features.blasters.boundingbox

import net.horizonsend.ion.server.features.blasters.boundingbox.utils.BoundingBoxUtil.within2DHeight
import net.horizonsend.ion.server.features.blasters.boundingbox.utils.BoundingBoxUtil.within2DWidth
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

class PlayerBoundingBox() : AbstractBoundingBox {
	private var bodyWidthRadius = 0.51
	private var bodyheight = 1.45
	private var headTopHeight = 1.95
	private val headTopCrouching = 1.49
	private val bodyheightCrouching = 1.0

	constructor(bodyheight: Double, bodyRadius: Double, headTopHeight: Double) : this() {
		this.bodyheight = bodyheight
		bodyWidthRadius = bodyRadius
		this.headTopHeight = headTopHeight
	}

	override fun intersects(shooter: Entity, check: Location, base: Entity): Boolean {
		val intersectsBodyWIDTH = within2DWidth(base, check, bodyWidthRadius, bodyWidthRadius)
		return if (!intersectsBodyWIDTH) false else intersectsHead(check, base) || intersectsBody(check, base)
	}

	override fun allowsHeadshots(): Boolean {
		return true
	}

	override fun intersectsHead(check: Location, base: Entity): Boolean {
		return if ((base as Player).isSneaking) {
			within2DHeight(base, check, headTopCrouching - bodyheightCrouching, bodyheightCrouching)
		} else within2DHeight(base, check, headTopHeight - bodyheight, bodyheight)
	}

	override fun intersectsBody(check: Location, base: Entity): Boolean {
		return if ((base as Player).isSneaking) {
			within2DHeight(base, check, bodyheightCrouching)
		} else within2DHeight(base, check, bodyheight)
	}

	override fun maximumCheckingDistance(base: Entity): Double {
		return bodyWidthRadius * 2
	}
}
