package net.horizonsend.ion.server.legacy.starshipweapon.projectile

import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.subsystem.weapon.projectile.ParticleProjectile
import net.starlegacy.util.mcName
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.util.Vector

class SonicMissileProjectile(
	starship: ActiveStarship,
	loc: Location,
	dir: Vector,
	shooter: Player?
) : ParticleProjectile(starship, loc, dir, shooter) {
	override val range: Double = starship.world.viewDistance.toDouble() * 16
	override var speed: Double = 200.0
	override val shieldDamageMultiplier: Int = 10
	override val thickness: Double = 0.2
	override val explosionPower: Float = 15f
	override val volume: Int = 10
	override val pitch: Float = 2.0f
	override val soundName: String = Sound.ENTITY_WARDEN_SONIC_BOOM.mcName

	override fun spawnParticle(x: Double, y: Double, z: Double, force: Boolean) {
		val offset = 0.0
		val count = 1
		val extra = 0.0
		val data = null
		loc.world.spawnParticle(Particle.SONIC_BOOM, x, y, z, count, offset, offset, offset, extra, data, force)
	}
}
