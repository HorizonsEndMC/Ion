package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.configuration.starship.IonTurretBalancing.IonTurretProjectileBalancing
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.status_effects.StarshipStatusEffect
import net.horizonsend.ion.server.features.starship.status_effects.StarshipStatusEffectTypes
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.ProjectileSource
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.helixAroundVector
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.damage.DamageType
import org.bukkit.util.Vector

class IonTurretProjectile(
	source: ProjectileSource,
	name: Component,
	loc: Location,
	dir: Vector,
	override val color: Color,
	shooter: Damager
): LaserProjectile<IonTurretProjectileBalancing>(source, name, loc, dir, shooter, DamageType.GENERIC) {

	override fun moveVisually(oldLocation: Location, newLocation: Location, travel: Double) {
		val vector = direction.clone().normalize().multiply(travel)
		val particle = Particle.DUST
		val dustOptions = Particle.DustOptions(color, particleThickness.toFloat() * 4f)

		helixAroundVector(oldLocation, vector, 0.3, 5, wavelength = 1.0) {
			location.world.spawnParticle(
				Particle.WAX_OFF,
				it,
				0,
				0.0,
				0.0,
				0.0,
				0.0,
				null,
				true
			)
		}

		location.world.spawnParticle(particle, location.x, location.y, location.z, 1, 0.0, 0.0, 0.0, 0.5, dustOptions, true)
	}

	override fun onImpactStarship(starship: ActiveStarship, impactLocation: Location) {
		starship.addStatusEffect(StarshipStatusEffect(
			StarshipStatusEffectTypes.DIRECT_CONTROL_SLOW,
			balancing.effectStrength,
			balancing.effectDurationMillis,
			shooter.starship,
		))
		starship.addStatusEffect(StarshipStatusEffect(
			StarshipStatusEffectTypes.CRUISE_SLOW,
			balancing.effectStrength,
			balancing.effectDurationMillis,
			shooter.starship,
		))
	}

	companion object {
		private const val SLOW_FACTOR = 0.1
		private const val SLOW_DURATION_SECONDS = 7L
	}
}
