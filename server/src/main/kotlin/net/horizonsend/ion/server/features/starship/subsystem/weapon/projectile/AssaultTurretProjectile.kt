package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.configuration.starship.AssaultTurretBalancing
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.ProjectileSource
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.circlePoints
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.helixAroundVector
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.rectangle
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.damage.DamageType
import org.bukkit.util.Vector

class AssaultTurretProjectile(
	source: ProjectileSource,
	name: Component,
	loc: Location,
	dir: Vector,
	override val color: Color,
	shooter: Damager,
	override val balancing: AssaultTurretBalancing.AssaultTurretProjectileBalancing,
	side: Boolean
): LaserProjectile<AssaultTurretBalancing.AssaultTurretProjectileBalancing>(source, name, loc, dir, shooter, DamageType.GENERIC) {
	val currentSide = side

	override fun spawnParticle(x: Double, y: Double, z: Double, force: Boolean) {
		val origin = Location(location.world, x, y, z)
		val forwardDirection = origin.direction.clone().normalize()
		val rightDirection = forwardDirection.clone().crossProduct(Vector(0.0, 1.5, 0.0)).normalize()
		val radius = 1.5

		if (currentSide) { spawnRightParticles(force, origin, forwardDirection, rightDirection, radius) }
		else { spawnLeftParticles(force, origin, forwardDirection, rightDirection, radius) }
	}

	fun spawnRightParticles(
		force: Boolean,
		origin: Location,
		forwardDirection: Vector,
		rightDirection: Vector,
		radius: Double
	) {
		val pointForward = origin.clone().add(forwardDirection.clone().multiply(radius))
		val pointBackward = origin.clone().subtract(forwardDirection.clone().multiply(radius))
		val pointRight = origin.clone().add(rightDirection.clone().multiply(radius))
		val midBackwardRight = pointBackward.clone().add(pointRight).multiply(0.5)
		val midForwardRight = pointForward.clone().add(pointRight).multiply(0.5)

		super.spawnParticle(pointRight.x, pointRight.y, pointRight.z, force)
		super.spawnParticle(pointBackward.x, pointBackward.y, pointBackward.z, force)
		super.spawnParticle(pointForward.x, pointForward.y, pointForward.z, force)
		super.spawnParticle(midForwardRight.x, midForwardRight.y, midForwardRight.z, force)
		super.spawnParticle(midBackwardRight.x, midBackwardRight.y, midBackwardRight.z, force)
	}

	fun spawnLeftParticles(
		force: Boolean,
		origin: Location,
		forwardDirection: Vector,
		rightDirection: Vector,
		radius: Double
	) {
		val pointForward = origin.clone().add(forwardDirection.clone().multiply(radius))
		val pointBackward = origin.clone().subtract(forwardDirection.clone().multiply(radius))
		val pointLeft = origin.clone().subtract(rightDirection.clone().multiply(radius))
		val midBackwardLeft = pointBackward.clone().add(pointLeft).multiply(0.5)
		val midForwardLeft = pointForward.clone().add(pointLeft).multiply(0.5)

		super.spawnParticle(pointLeft.x, pointLeft.y, pointLeft.z, force)
		super.spawnParticle(pointBackward.x, pointBackward.y, pointBackward.z, force)
		super.spawnParticle(pointForward.x, pointForward.y, pointForward.z, force)
		super.spawnParticle(midBackwardLeft.x, midBackwardLeft.y, midBackwardLeft.z, force)
		super.spawnParticle(midForwardLeft.x, midForwardLeft.y, midForwardLeft.z, force)
	}
}
