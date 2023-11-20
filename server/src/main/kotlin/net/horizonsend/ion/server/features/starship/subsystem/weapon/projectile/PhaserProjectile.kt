package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.util.Vector
import java.util.concurrent.TimeUnit

class PhaserProjectile(
	starship: ActiveStarship?,
	loc: Location,
	dir: Vector,
	shooter: Damager
) : ParticleProjectile(starship, loc, dir, shooter) {
	override val balancing: StarshipWeapons.ProjectileBalancing = starship?.balancing?.weapons?.phaser ?: IonServer.starshipBalancing.nonStarshipFired.phaser
	override val range: Double = balancing.range
	override var speed: Double = balancing.speed
	override val starshipShieldDamageMultiplier = balancing.starshipShieldDamageMultiplier
	override val areaShieldDamageMultiplier: Double = balancing.areaShieldDamageMultiplier
	override val explosionPower: Float = balancing.explosionPower
	override val volume: Int = balancing.volume
	override val pitch: Float = balancing.pitch
	override val soundName: String = balancing.soundName

	private val speedUpTime = TimeUnit.MILLISECONDS.toNanos(500L)
	private val speedUpSpeed = 1000.0

	override fun moveVisually(oldLocation: Location, newLocation: Location, travel: Double) {
		println("Old location: $oldLocation")
		println("New location: $newLocation")
		println("Travel: $travel")

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
