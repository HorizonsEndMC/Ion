package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.configuration.starship.PhaserBalancing.PhaserProjectileBalancing
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy.PhaserStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.ProjectileSource
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.util.Vector
import java.util.concurrent.TimeUnit

class PhaserProjectile(
	source: ProjectileSource,
	name: Component,
	loc: Location,
	dir: Vector,
	shooter: Damager
) : ParticleProjectile<PhaserProjectileBalancing>(source, name, loc, dir, shooter, PhaserStarshipWeaponMultiblock.damageType) {
	override var speed: Double = balancing.speed; get() = balancing.speed

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

		location.world.spawnParticle(Particle.SOUL_FIRE_FLAME, x, y, z, count, offset, offset, offset, extra, data, force)
	}
}
