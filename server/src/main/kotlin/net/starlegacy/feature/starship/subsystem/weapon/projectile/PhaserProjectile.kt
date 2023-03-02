package net.starlegacy.feature.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.IonServer
import net.starlegacy.feature.starship.active.ActiveStarship
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import java.util.concurrent.TimeUnit

class PhaserProjectile(
	starship: ActiveStarship,
	loc: Location,
	dir: Vector,
	shooter: Player?
) : ParticleProjectile(starship, loc, dir, shooter) {
	override val range: Double = IonServer.balancing.starshipWeapons.Phaser.range
	override var speed: Double = IonServer.balancing.starshipWeapons.Phaser.speed
	override val shieldDamageMultiplier: Int = IonServer.balancing.starshipWeapons.Phaser.shieldDamageMultiplier
	override val thickness: Double = IonServer.balancing.starshipWeapons.Phaser.thickness
	override val explosionPower: Float = IonServer.balancing.starshipWeapons.Phaser.explosionPower
	override val volume: Int = IonServer.balancing.starshipWeapons.Phaser.volume
	override val pitch: Float = IonServer.balancing.starshipWeapons.Phaser.pitch
	override val soundName: String = IonServer.balancing.starshipWeapons.Phaser.soundName

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
