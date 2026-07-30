package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.configuration.starship.StarshipTrackingProjectileBalancing
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.ProjectileSource
import net.kyori.adventure.key.Key.key
import net.kyori.adventure.sound.Sound.Source
import net.kyori.adventure.sound.Sound.sound
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.damage.DamageType
import org.bukkit.util.RayTraceResult
import org.bukkit.util.Vector
import org.checkerframework.checker.units.qual.Current
import kotlin.collections.minusAssign
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
	override var speed: Double = balancing.speed

	private var previousTargetPos: Vector? = null
	private val smoothingAlpha = 2.0 / (35.0 + 1.0)
	private var smoothedTargetSpeed: Vector = Vector(0.0, 0.0, 0.0)
	private var turnRate = 1.0

	protected val maxDegrees: Double get() = balancing.maxDegrees

	open fun calculateTarget() = targetBase.clone().add(getTargetOrigin())

	override fun fire() {
		processTarget()

		super.fire()
	}

	private fun processTarget() {
		val targetOffset = originalTarget.clone().subtract(location.toVector())
		val targetShips = ActiveStarships.getInWorld(location.world).filter {
			it.centerOfMass.toCenterVector().distanceSquared(location.toVector()) <= range * range &&
				it != shooter.starship

		}
		val angles =
			targetShips.map { it.centerOfMass.toCenterVector().subtract(location.toVector()).angle(targetOffset) }
		val minAngleIndex = angles.withIndex().minByOrNull { it.value }?.index
		val targetShip =
			if (minAngleIndex != null && angles[minAngleIndex] <= maxTrackingRadius) targetShips[minAngleIndex] else null
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
		if (blockCount > 3000) {
			targetBase = originalTarget.clone().add(getTargetOrigin())
		}
		//The zero vector here denotes no change from center of weight.
		else targetBase = Vector()
	}

	override fun tick() {
		super.tick()
		// slows down projectile over time (and it's turning rate)
		speed -= 0.5
		turnRate -= 0.025
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
			val impacted = tryImpact(RayTraceResult(calculateTarget()), calculateTarget().toLocation(location.world))
			if (impacted) {
				onImpact()
				return onDespawn()
			}
			// Speed calculations for Lead
			val currentTargetPos = calculateTarget()
			val prevPos = previousTargetPos
			if (prevPos != null) {
				val targetSpeed = currentTargetPos.clone().subtract(prevPos).multiply(20.0)
				smoothedTargetSpeed = smoothedTargetSpeed.multiply(1.0 - smoothingAlpha)
					.add(targetSpeed.multiply(smoothingAlpha))
				previousTargetPos = currentTargetPos.clone()
			} else {
				smoothedTargetSpeed = Vector(0.0, 0.0, 0.0)
				previousTargetPos = currentTargetPos.clone()
			}
			val distanceToTarget = this.location.toVector().distance(currentTargetPos)
			val timeToTarget = distanceToTarget / speed

			val predictionOffset = smoothedTargetSpeed.clone()?.multiply(timeToTarget) ?: Vector(0.0, 0.0, 0.0)


			val targetDirection = calculateTarget()
				.subtract(location.toVector())
				.add(predictionOffset)
				.normalize()
			direction = adjust(direction, targetDirection, Math.toRadians(turnRate * maxDegrees * delta))
		}
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
