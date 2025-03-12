package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.common.utils.miscellaneous.squared
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.damage.DamageType
import org.bukkit.util.Vector
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

abstract class TrackingLaserProjectile(
	starship: ActiveStarship?,
	name: Component,
	loc: Location,
	dir: Vector,
	shooter: Damager,
	private val originalTarget: Vector,
	private val aimDistance: Int,
	damageType: DamageType
) : LaserProjectile(starship, name, loc, dir, shooter, damageType) {
	val maxTrackingRadius = 0.15
	private lateinit var getTargetOrigin: () -> Vector
	private lateinit var targetBase: Vector

	protected abstract val maxDegrees: Double

	private fun calculateTarget() = getTargetOrigin().clone()

	override fun fire() {
		processTarget()

		super.fire()
	}

	private fun processTarget() {
		val targetOffset = originalTarget.clone().subtract(loc.toVector())
		val targetShips = ActiveStarships.getInWorld(loc.world).filter {
			it.centerOfMass.toCenterVector().distanceSquared(loc.toVector()) <= range*range &&
				it != shooter.starship

		}
		val angles = targetShips.map { it.centerOfMass.toCenterVector().subtract(loc.toVector()).angle(targetOffset) }
		val minAngleIndex = angles.withIndex().minByOrNull { it.value }?.index
		val targetShip = if (minAngleIndex != null && angles[minAngleIndex] <= maxTrackingRadius) targetShips[minAngleIndex] else null
		getTargetOrigin = {
			targetShip?.centerOfMass?.toCenterVector() ?: originalTarget
		}
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
