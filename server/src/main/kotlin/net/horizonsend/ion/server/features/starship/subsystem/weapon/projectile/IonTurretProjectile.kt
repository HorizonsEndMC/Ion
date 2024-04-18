package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.miscellaneous.utils.helixAroundVector
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.util.Vector

class IonTurretProjectile(
		ship: ActiveStarship?,
		loc: Location,
		dir: Vector,
		override val speed: Double,
		override val color: Color,
		override val range: Double,
		override val particleThickness: Double,
		override val explosionPower: Float,
		override val starshipShieldDamageMultiplier: Double,
		override val areaShieldDamageMultiplier: Double,
		override val soundName: String,
		override val balancing: StarshipWeapons.ProjectileBalancing?,
		shooter: Damager

): LaserProjectile(ship, loc, dir, shooter) {

	override val volume: Int = (range / 16).toInt()

	override fun moveVisually(oldLocation: Location, newLocation: Location, travel: Double) {
		val vector = dir.clone().normalize().multiply(travel)
		val particle = Particle.REDSTONE
		val dustOptions = Particle.DustOptions(color, particleThickness.toFloat() * 4f)

		helixAroundVector(oldLocation, vector, 0.3, 20, wavelength = 1.0) {
			loc.world.spawnParticle(
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

		loc.world.spawnParticle(particle, loc.x, loc.y, loc.z, 1, 0.0, 0.0, 0.0, 0.5, dustOptions, true)
	}

	override fun onImpactStarship(starship: ActiveStarship, impactLocation: Location) {
		val shipsThrusters = starship.thrusters

		for (thruster in shipsThrusters) {
			if (impactLocation.distance(thruster.pos.toLocation(starship.world)) > 8) continue

			thruster.lastIonTurretLimited = System.currentTimeMillis()
		}
	}
}
