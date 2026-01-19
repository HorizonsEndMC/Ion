package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.configuration.starship.LightLogisticsCannonBalancing.LightLogisticsCannonProjectileBalancing
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.shield.ShieldSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.ProjectileSource
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.damage.DamageType
import org.bukkit.entity.Player
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

		pointLeft.world.spawnParticle(Particle.SCRAPE, pointLeft.x,pointLeft.y,pointLeft.z, 1, 0.0, 0.0 , 0.0, 0.0, null, true)
		pointLeft.world.spawnParticle(Particle.SCRAPE, pointRight.x,pointRight.y,pointRight.z, 1, 0.0, 0.0 , 0.0, 0.0, null, true)
		pointLeft.world.spawnParticle(Particle.GLOW, pointBackward.x,pointBackward.y,pointBackward.z, 1, 0.0, 0.0 , 0.0, 0.0, null, true)
		pointLeft.world.spawnParticle(Particle.GLOW, pointForward.x,pointForward.y,pointForward.z, 1, 0.0, 0.0 , 0.0, 0.0, null, true)

	}
	override fun onImpactStarship(starship: ActiveStarship, impactLocation: Location) {
		if (starship.controller !is Player) return
		val shooter = shooter.starship?.controller ?: return
		if (shooter.starship.controller !is Player) return
		if (PlayerCache[starship.controller as Player].frontierNationOid != PlayerCache[shooter.starship.controller as Player].frontierNationOid) return
		if (starship.type == StarshipType.LOGISTICS_CRUISER || starship.type == StarshipType.LOGISTICS_CORVETTE)

		for (shield: ShieldSubsystem in starship.shields) {
			if (impactLocation.distance(shield.pos.toLocation(starship.world)) > 10) continue
			if (!shield.isReinforcementActive()) shield.power += balancing.shieldBoostFactor
		}
	}
}
