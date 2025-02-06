package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.configuration.starship.StarshipTrackingProjectileBalancing
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.ProjectileSource
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.damage.DamageType
import org.bukkit.util.Vector
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

abstract class TrackingLaserProjectile<B : StarshipTrackingProjectileBalancing>(
	source: ProjectileSource,
	name: Component,
	loc: Location,
	dir: Vector,
	shooter: Damager,
	private val originalTarget: Vector,
	private val aimDistance: Int,
	damageType: DamageType
) : LaserProjectile<B>(source, name, loc, dir, shooter, damageType) {
	val maxTrackingRadius = 0.15
	private lateinit var getTargetOrigin: () -> Vector
	private lateinit var targetBase: Vector
	var track: Boolean = true

	protected val maxDegrees: Double get() = balancing.maxDegrees

	private fun calculateTarget() = getTargetOrigin().clone()

	override fun fire() {
		processTarget()

		super.fire()
	}

	private fun processTarget() {
		val targetOffset = originalTarget.clone().subtract(location.toVector())
		val targetShips = ActiveStarships.getInWorld(location.world).filter {
			it.centerOfMass.toCenterVector().distanceSquared(location.toVector()) <= range*range &&
				it != shooter.starship

		}
		val angles = targetShips.map { it.centerOfMass.toCenterVector().subtract(location.toVector()).angle(targetOffset) }
		val minAngleIndex = angles.withIndex().minByOrNull { it.value }?.index
		val targetShip = if (minAngleIndex != null && angles[minAngleIndex] <= maxTrackingRadius) targetShips[minAngleIndex] else null
		getTargetOrigin = {
			targetShip?.centerOfMass?.toCenterVector() ?: originalTarget
		}

		/*
			If the target ship is smaller than 3000 blocks. We target the center of weight.
			Otherwise, we use the block that was targeted by the player.
			This maintains the strength of tracked laser projectiles against smaller ships.
			However, removes their perfect tracking of the core of larger ships.
		*/
		val blockCount = targetShip?.currentBlockCount ?: 0
		if (blockCount>3000) {
			targetBase = originalTarget.clone().add(getTargetOrigin())
		}
		//The zero vector here denotes no change from center of weight.
		else targetBase = Vector()
	}

	override fun tick() {
		super.tick()
		if (track) adjustDirection()
	}

	private fun adjustDirection() {
		if (distance < aimDistance) {
			return
		}
		/*
		If our projectile is within x blocks of the targeted block.
		We prematurely detonate the projectile at the target block
		this prevents a rotation from screwing up the missiles tracking. Preventing dud impacts.
		 */
		if (this.location.toVector().distanceSquared(calculateTarget()) <= balancing.detonationRange) {
			impact(calculateTarget().toLocation(location.world), null, null)
			return
		}

		val targetDirection = calculateTarget()
			.subtract(location.toVector())
			.normalize()
		direction = adjust(direction, targetDirection, Math.toRadians(maxDegrees * delta))
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
