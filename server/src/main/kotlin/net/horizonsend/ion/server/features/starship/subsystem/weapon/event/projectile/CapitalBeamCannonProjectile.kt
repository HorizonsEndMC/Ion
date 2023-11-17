package net.horizonsend.ion.server.features.starship.subsystem.weapon.event.projectile

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.ParticleProjectile
import net.horizonsend.ion.server.miscellaneous.utils.helixAroundVector
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.util.Vector

class CapitalBeamCannonProjectile(
	starship: ActiveStarship?,
	loc: Location,
	dir: Vector,
	shooter: Damager
) : ParticleProjectile(starship, loc, dir, shooter) {
	override val balancing: StarshipWeapons.ProjectileBalancing = starship?.balancing?.weapons?.capitalBeam ?: IonServer.starshipBalancing.nonStarshipFired.capitalBeam
	override val range: Double = balancing.range
	override var speed: Double = balancing.speed
	override val shieldDamageMultiplier: Int = balancing.shieldDamageMultiplier
	override val explosionPower: Float = balancing.explosionPower
	override val volume: Int = balancing.volume
	override val pitch: Float = balancing.pitch
	override val soundName: String = balancing.soundName

	override fun moveVisually(oldLocation: Location, newLocation: Location, travel: Double) {
		val vector = dir.clone().normalize().multiply(travel)

		for (location in helixAroundVector(oldLocation, vector, 5.0, 150, wavelength = 1.0)) {
			loc.world.spawnParticle(
				Particle.BLOCK_MARKER,
				location,
				0,
				0.5,
				0.5,
				0.5,
				0.0,
				FlamethrowerProjectile.fire,
				true
			)
		}
	}

	override fun spawnParticle(x: Double, y: Double, z: Double, force: Boolean) {
		TODO("Not yet implemented")
	}
}
