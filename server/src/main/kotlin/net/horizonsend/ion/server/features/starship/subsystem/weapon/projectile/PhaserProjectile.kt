package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.util.Vector
import java.util.concurrent.TimeUnit

class PhaserProjectile(
	starship: ActiveStarship,
	loc: Location,
	dir: Vector,
	shooter: Damager
) : ParticleProjectile(starship, loc, dir, shooter) {
	override val range: Double = IonServer.balancing.starshipWeapons.phaser.range
	override var speed: Double = IonServer.balancing.starshipWeapons.phaser.speed
	override val shieldDamageMultiplier: Int = IonServer.balancing.starshipWeapons.phaser.shieldDamageMultiplier
	override val thickness: Double = IonServer.balancing.starshipWeapons.phaser.thickness
	override val explosionPower: Float = IonServer.balancing.starshipWeapons.phaser.explosionPower
	override val volume: Int = IonServer.balancing.starshipWeapons.phaser.volume
	override val pitch: Float = IonServer.balancing.starshipWeapons.phaser.pitch
	override val soundName: String = IonServer.balancing.starshipWeapons.phaser.soundName

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
