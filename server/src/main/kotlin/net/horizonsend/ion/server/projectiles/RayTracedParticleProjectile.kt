package net.horizonsend.ion.server.projectiles

import net.horizonsend.ion.server.projectiles.constructors.RayTracedProjectile
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.entity.Entity

class RayTracedParticleProjectile(
	override val location: Location,
	override val speed: Double,
	override val shooter: Entity,
	override var damage: Double,
	override val damageFalloffMultiplier: Double,
	override val shouldPassThroughEntities: Boolean,
	override val size: Double,
	override val shouldBypassHitTicks: Boolean,
	override val range: Double,
	val particle: Particle,
	private val dustOptions: DustOptions?,
) : RayTracedProjectile() {
	private var directionVector = location.direction.clone().multiply(speed)

	override fun tick(): Boolean {
		/**
		 * Every tick, this function will repeat the code below.
		 * If it returns true, the projectile manager will delete the projectile.
		 *
		 * True if:
		 * Distance > Range
		 * Location is unloaded
		 * Projectile collides
		 **/

		ticks += 1

		if (ticks * speed > range) return true

		location.add(directionVector)

		if (!location.isChunkLoaded) return true

		if (dustOptions != null) {
			location.world.spawnParticle(particle, location, 1, 0.0, 0.0, 0.0, 0.0, dustOptions, true)
		} else location.world.spawnParticle(particle, location, 1, 0.0, 0.0, 0.0, 0.0, null, true)

		if (rayCastTick()) return true

		calculateDamage()

		return false
	}
}