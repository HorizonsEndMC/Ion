package net.horizonsend.ion.server.projectiles

import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions

class Projectile(
	private val location: Location,
	private val dustOptions: DustOptions,
	private val iterationsPerTick: Int,
	distancePerIteration: Double
) {
	private var directionVector = location.direction.multiply(distancePerIteration)

	fun tick(): Boolean {
		repeat(iterationsPerTick) {
			location.add(directionVector)
			if (!location.isChunkLoaded) return true
			location.world.spawnParticle(Particle.REDSTONE, location, 1, 0.0, 0.0, 0.0, 0.0, dustOptions, true)
		}

		return false
	}
}