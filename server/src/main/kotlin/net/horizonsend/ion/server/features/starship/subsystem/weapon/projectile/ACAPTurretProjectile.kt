package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.configuration.starship.ACAPTurretBalancing
import net.horizonsend.ion.server.configuration.starship.QuadTurretBalancing
import net.horizonsend.ion.server.features.custom.items.type.weapon.blaster.RayTracedParticleProjectile
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.damager.event.ImpactStarshipEvent
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.ProjectileSource
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.circlePoints
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.damage.DamageType
import org.bukkit.util.Vector
import kotlin.math.roundToInt

class ACAPTurretProjectile(
	source: ProjectileSource,
	name: Component,
	loc: Location,
	dir: Vector,
	override val color: Color,
	shooter: Damager,
	override val balancing: ACAPTurretBalancing.ACAPTurretProjectileBalancing
): LaserProjectile<ACAPTurretBalancing.ACAPTurretProjectileBalancing>(source, name, loc, dir, shooter, DamageType.GENERIC) {
	var tick = 0
	val particleData = Particle.DustTransition(
		Color.fromARGB(255, 0, 139, 139),
		shooter.color,
		2.0f
	)

	override fun spawnParticle(x: Double, y: Double, z: Double, force: Boolean) {
		val particle1 = Particle.DUST_COLOR_TRANSITION
		val particle2 = Particle.SONIC_BOOM
		location.world.spawnParticle(particle2, x, y, z, 1, 0.0, 0.0, 0.0, 0.0, null, force)
		location.world.spawnParticle(particle1, x, y, z, 1, 0.0, 0.0, 0.0, 0.0, particleData, force)
	}


	override fun onImpactStarship(starship: ActiveStarship, impactLocation: Location) {
		if (starship.initialBlockCount > 12000)
		impactLocation.createExplosion(6.0f)
		addToDamagers(impactLocation.world, impactLocation.block, shooter, 6.0f.roundToInt(), explosionOccurred = false, runStarshipImpactEvent = false)
	}
}
