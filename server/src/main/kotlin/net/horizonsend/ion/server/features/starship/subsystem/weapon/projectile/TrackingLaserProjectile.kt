package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.configuration.starship.StarshipTrackingProjectileBalancing
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.ProjectileSource
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockPos
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toLocation
import net.kyori.adventure.text.Component
import net.minecraft.core.BlockPos
import org.bukkit.Location
import org.bukkit.damage.DamageType
import org.bukkit.util.RayTraceResult
import kotlin.math.PI
import org.bukkit.util.Vector
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.roundToInt
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
	open var track: Boolean = true
	override var speed: Double = 0.0

	private var previousTargetPos: Vector? = null
	private val smoothingAlpha = 2.0 / (35.0 + 1.0)
	private var smoothedTargetSpeed: Vector = Vector(0.0, 0.0, 0.0)
	private var turnRate = 1.0

	protected val maxDegrees: Double get() = balancing.maxDegrees
	protected val thetaList = when(balancing.detonationRange){
		in 0.0..2.0 -> thetaList90
		in 3.0..8.0 -> thetaList60
		else -> thetaList30
	}

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
		//missile acceleration and less manuvering
		speed = (speed + balancing.acceleration).coerceAtMost(balancing.speed)
		turnRate = (turnRate - 0.005).coerceAtLeast(balancing.turnRate)
		if (track) adjustDirection()
	}

	private fun adjustDirection() {
		if (distance < aimDistance) {
			return
		}

		/*
		In the plane perpendicular to the missile's direction, we check in r Radius, and in increments of theta given in
		thetaList. We check every blockPos until we find a blockPos that has a block, and a block that is solid.
		We then try to impact at that blockPos, impacting and despawning the missile if successful.
		 */

		val up = if (abs(direction.y) < 0.99) Vector(0.0, 1.0, 0.0) else Vector(1.0, 0.0, 0.0)
		val basisU = direction.crossProduct(up).normalize()
		val basisV = direction.crossProduct(basisU).normalize()
		for(r in 0..balancing.detonationRange.roundToInt()) {
			for (theta in thetaList){
				val offset = basisU.multiply(cos(theta) * r).add(basisV.multiply(sin(theta) * r))
				val blockPosToCheck = location.toBlockPos().offset(offset.blockX,offset.blockY,offset.blockZ)
				//Use .isSolid so something like vines or grass cant detonate the missile
				val blockAtLocation = location.world.getBlockAt(blockPosToCheck.x,blockPosToCheck.y,blockPosToCheck.z)
				if(blockAtLocation != null) {
					if (blockAtLocation.isSolid) {
						val impacted = tryImpact(
							RayTraceResult(
								Vector(blockPosToCheck.x, blockPosToCheck.y, blockPosToCheck.z),
								blockAtLocation,
								null
							),
							blockAtLocation.location
						)
						if (impacted) {
							onImpact()
							return onDespawn()
						}
					}
				}
			}
		}
		/*
		If our projectile is within x blocks of the targeted block.
		We prematurely detonate the projectile at the target block
		this prevents a rotation from screwing up the missiles tracking. Preventing dud impacts.
	 	*/

		if (this.location.toVector().distance(calculateTarget()) <= balancing.detonationRange) {
			impact(calculateTarget().toLocation(location.world),null,null)
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

		val predictionOffset = smoothedTargetSpeed.clone().multiply(timeToTarget) ?: Vector(0.0, 0.0, 0.0)


		val targetDirection = calculateTarget()
			.subtract(location.toVector())
			.add(predictionOffset)
			.normalize()
		direction = adjust(direction, targetDirection, Math.toRadians(turnRate * maxDegrees * delta))
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
	companion object {
		//list of angles to check for detonation range in the plane perpendicular to the missile trajectory.
		//Given in radians every given degrees. This avoids checking too many blocks, but still allows good coverage.
		//Recommend using 90 radii below 3
		//Recommend using 30 for radii above 8
		val thetaList90 = listOf(PI/2,0.0,-PI/2,-PI)
		val thetaList60 = listOf(PI/3,(2*PI)/3,0.0,-PI/3,-(2*PI)/3,-PI,)
		val thetaList30 = listOf(
			0.0, PI/6, PI/3, PI/2, (2*PI)/3, (5*PI)/6, PI,
			-PI/6, -PI/3, -PI/2, -(2*PI)/3, -(5*PI)/6
		)	}
}
