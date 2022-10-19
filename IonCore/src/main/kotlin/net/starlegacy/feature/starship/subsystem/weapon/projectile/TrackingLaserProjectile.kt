package net.starlegacy.feature.starship.subsystem.weapon.projectile

import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.active.ActiveStarships
import net.starlegacy.util.squared
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.util.Vector

abstract class TrackingLaserProjectile(
	starship: ActiveStarship?,
	loc: Location,
	dir: Vector,
	shooter: Player?,
	private val originalTarget: Vector,
	private val aimDistance: Int
) : LaserProjectile(starship, loc, dir, shooter) {
	private lateinit var getTargetOrigin: () -> Vector
	private lateinit var targetBase: Vector

	protected abstract val maxDegrees: Double

	private fun calculateTarget() = targetBase.clone().add(getTargetOrigin())

	override fun fire() {
		processTarget()

		super.fire()
	}

	private fun processTarget() {
		val targetShip = ActiveStarships.findByBlock(originalTarget.toLocation(loc.world))
		getTargetOrigin = {
			targetShip?.centerOfMass?.toCenterVector() ?: originalTarget
		}
		targetBase = originalTarget.clone().subtract(getTargetOrigin())
	}

	override fun tick() {
		super.tick()
		adjustDirection()
	}

	private fun adjustDirection() {
		if (distance < aimDistance) {
			return
		}

		if (this.loc.toVector().distanceSquared(calculateTarget()) <= 1.5.squared()) {
			impact(this.loc, null, null)
			return
		}

		val targetDirection = calculateTarget()
			.subtract(loc.toVector())
			.normalize()
		dir = adjust(dir, targetDirection, Math.toRadians(maxDegrees * delta))
	}

	private fun adjust(start: Vector, end: Vector, maxRadians: Double): Vector {
		if (start.distance(end) < 0.01) {
			return end
		}
		val percent = (maxRadians / start.angle(end)).coerceAtMost(1.0)
		val dot = start.dot(end).coerceIn(-1.0, 1.0)
		val theta = acos(dot) * percent
		val relativeVec = end.subtract(start.multiply(dot)).normalize()
		return start.multiply(cos(theta)).add(relativeVec.multiply(sin(theta))).normalize()
	}
}
