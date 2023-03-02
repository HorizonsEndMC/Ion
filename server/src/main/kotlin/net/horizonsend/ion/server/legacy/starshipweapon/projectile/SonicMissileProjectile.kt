package net.horizonsend.ion.server.legacy.starshipweapon.projectile

import net.horizonsend.ion.server.IonServer
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.subsystem.weapon.projectile.ParticleProjectile
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.util.Vector

class SonicMissileProjectile(
	starship: ActiveStarship,
	loc: Location,
	dir: Vector,
	shooter: Player?
) : ParticleProjectile(starship, loc, dir, shooter) {
	override val range: Double = IonServer.balancing.starshipWeapons.SonicMissile.range
	override var speed: Double = IonServer.balancing.starshipWeapons.SonicMissile.speed
	override val shieldDamageMultiplier: Int = IonServer.balancing.starshipWeapons.SonicMissile.shieldDamageMultiplier
	override val thickness: Double = IonServer.balancing.starshipWeapons.SonicMissile.thickness
	override val explosionPower: Float = IonServer.balancing.starshipWeapons.SonicMissile.explosionPower
	override val volume: Int = IonServer.balancing.starshipWeapons.SonicMissile.volume
	override val pitch: Float = IonServer.balancing.starshipWeapons.SonicMissile.pitch
	override val soundName: String = IonServer.balancing.starshipWeapons.SonicMissile.soundName

	override fun spawnParticle(x: Double, y: Double, z: Double, force: Boolean) {
		val offset = 0.0
		val count = 1
		val extra = 0.0
		val data = null
		loc.world.spawnParticle(Particle.SONIC_BOOM, x, y, z, count, offset, offset, offset, extra, data, force)
	}
}
