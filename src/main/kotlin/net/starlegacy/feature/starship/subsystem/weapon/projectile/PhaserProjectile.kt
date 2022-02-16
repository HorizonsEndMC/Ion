package net.starlegacy.feature.starship.subsystem.weapon.projectile

import java.util.concurrent.TimeUnit
import net.starlegacy.feature.starship.active.ActiveStarship
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.util.Vector

class PhaserProjectile(
	starship: ActiveStarship,
	loc: Location,
	dir: Vector,
	shooter: Player?
) : ParticleProjectile(starship, loc, dir, shooter) {
	override val range: Double = 140.0
	override var speed: Double = 1.0
	override val shieldDamageMultiplier: Int = 55
	override val thickness: Double = 0.2
	override val explosionPower: Float = 2.0f
	override val volume: Int = 10
	override val pitch: Float = 2.0f
	override val soundName: String = "starship.weapon.plasma_cannon.shoot"

	private val speedUpTime = TimeUnit.MILLISECONDS.toNanos(500L)
	private val speedUpSpeed = 1000.0

	override fun moveVisually(oldLocation: Location, newLocation: Location, travel: Double) {
		super.moveVisually(oldLocation, newLocation, travel)

		if (System.nanoTime() - this.firedAtNanos > this.speedUpTime) {
			this.speed = this.speedUpSpeed
		}
	}

	override fun spawnParticle(x: Double, y: Double, z: Double, force: Boolean) {
		val offset = 0.0
		val count = 1
		val extra = 0.0
		val data = null
		loc.world.spawnParticle(Particle.SOUL_FIRE_FLAME, x, y, z, count, offset, offset, offset, extra, data, force)
	}
}
