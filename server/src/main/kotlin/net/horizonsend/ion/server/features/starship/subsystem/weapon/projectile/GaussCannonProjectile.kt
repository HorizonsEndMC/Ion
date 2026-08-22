package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.configuration.starship.GaussCannonBalancing
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.ProjectileSource
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.alongVector
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.circlePoints
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.damage.DamageType
import org.bukkit.util.Vector
import kotlin.math.roundToInt

class GaussCannonProjectile(
	source: ProjectileSource,
	name: Component,
	loc: Location,
	dir: Vector,
	override val color: Color,
	shooter: Damager,
	override val balancing: GaussCannonBalancing.GaussCannonProjectileBalancing
): LaserProjectile<GaussCannonBalancing.GaussCannonProjectileBalancing>(source, name, loc, dir, shooter, DamageType.GENERIC) {

	override fun spawnParticle(x: Double, y: Double, z: Double, force: Boolean) {
		val origin = Location(location.world, x, y, z)
		val forwardDirection = origin.direction.clone().normalize()
		val rightDirection = forwardDirection.clone().crossProduct(Vector(0, 1, 0)).normalize()
		val radius = 1.0

		val pointForward = origin.clone().add(forwardDirection.clone().multiply(radius))
		val pointBackward = origin.clone().subtract(forwardDirection.clone().multiply(radius))
		val pointRight = origin.clone().add(rightDirection.clone().multiply(radius))
		val pointLeft = origin.clone().subtract(rightDirection.clone().multiply(radius))

		super.spawnParticle(pointLeft.x, pointLeft.y, pointLeft.z, force)
		super.spawnParticle(pointRight.x, pointRight.y, pointRight.z, force)
		super.spawnParticle(pointBackward.x, pointBackward.y, pointBackward.z, force)
		super.spawnParticle(pointForward.x, pointForward.y, pointForward.z, force)
	}
}
