package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.configuration.starship.LightLogisticsCannonBalancing.LightLogisticsCannonProjectileBalancing
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.shield.ShieldSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.ProjectileSource
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.damage.DamageType
import org.bukkit.util.Vector

class LightLogisticsProjectile(
	source: ProjectileSource,
	name: Component,
	loc: Location,
	dir: Vector,
	shooter: Damager
) : LaserProjectile<LightLogisticsCannonProjectileBalancing>(source, name, loc, dir, shooter, DamageType.GENERIC) {
	override val color: Color get() = shooter.color

	override fun spawnParticle(x: Double, y: Double, z: Double, force: Boolean) {
		val origin = Location(location.world, x, y, z)
		val forwardDirection = origin.direction.clone().normalize()
		val rightDirection = forwardDirection.clone().crossProduct(Vector(0, 1,0)).normalize()
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
	override fun onImpactStarship(starship: ActiveStarship, impactLocation: Location) {
		for (shield: ShieldSubsystem in starship.shields) {
			if (shield.isReinforcementEnabled) continue
			shield.power += balancing.shieldBoostFactor
		}
	}
}
