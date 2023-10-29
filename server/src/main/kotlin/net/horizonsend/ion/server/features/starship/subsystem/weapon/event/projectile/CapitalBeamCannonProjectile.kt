package net.horizonsend.ion.server.features.starship.subsystem.weapon.event.projectile

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.controllers.Controller
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.ParticleProjectile
import net.horizonsend.ion.server.miscellaneous.utils.helixAroundVector
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.util.Vector

class CapitalBeamCannonProjectile(
	starship: ActiveStarship?,
	loc: Location,
	dir: Vector,
	shooter: Controller?
) : ParticleProjectile(starship, loc, dir, shooter) {
	override val range: Double = IonServer.balancing.starshipWeapons.capitalBeam.range
	override var speed: Double = IonServer.balancing.starshipWeapons.capitalBeam.speed
	override val shieldDamageMultiplier: Int = IonServer.balancing.starshipWeapons.capitalBeam.shieldDamageMultiplier
	override val thickness: Double = IonServer.balancing.starshipWeapons.capitalBeam.thickness
	override val explosionPower: Float = IonServer.balancing.starshipWeapons.capitalBeam.explosionPower
	override val volume: Int = IonServer.balancing.starshipWeapons.capitalBeam.volume
	override val pitch: Float = IonServer.balancing.starshipWeapons.capitalBeam.pitch
	override val soundName: String = IonServer.balancing.starshipWeapons.capitalBeam.soundName

	override fun moveVisually(oldLocation: Location, newLocation: Location, travel: Double) {
		val vector = dir.clone().normalize().multiply(travel)
		println(travel)

		for (location in helixAroundVector(oldLocation, vector, 5.0, 150)) {

			loc.world.spawnParticle(
				Particle.BLOCK_MARKER,
				location,
				0,
				0.5,
				0.5,
				0.5,
				0.0,
				FlamethrowerProjectile.fire, true
			)
		}
	}

	override fun spawnParticle(x: Double, y: Double, z: Double, force: Boolean) {
		TODO("Not yet implemented")
	}
}
