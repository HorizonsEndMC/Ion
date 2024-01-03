package net.horizonsend.ion.server.features.starship.subsystem.weapon.event.projectile

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.starship.Damager
import net.horizonsend.ion.server.features.starship.control.controllers.Controller
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.ParticleProjectile
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.util.Vector

class SonicMissileProjectile(
	starship: ActiveStarship,
	loc: Location,
	dir: Vector,
	shooter: Damager
) : ParticleProjectile(starship, loc, dir, shooter) {
	override val range: Double = IonServer.balancing.starshipWeapons.sonicMissile.range
	override var speed: Double = IonServer.balancing.starshipWeapons.sonicMissile.speed
	override val shieldDamageMultiplier: Int = IonServer.balancing.starshipWeapons.sonicMissile.shieldDamageMultiplier
	override val thickness: Double = IonServer.balancing.starshipWeapons.sonicMissile.thickness
	override val explosionPower: Float = IonServer.balancing.starshipWeapons.sonicMissile.explosionPower
	override val volume: Int = IonServer.balancing.starshipWeapons.sonicMissile.volume
	override val pitch: Float = IonServer.balancing.starshipWeapons.sonicMissile.pitch
	override val soundName: String = IonServer.balancing.starshipWeapons.sonicMissile.soundName

	override fun spawnParticle(x: Double, y: Double, z: Double, force: Boolean) {
		val offset = 0.0
		val count = 1
		val extra = 0.0
		val data = null
		loc.world.spawnParticle(Particle.SONIC_BOOM, x, y, z, count, offset, offset, offset, extra, data, force)
	}
}
