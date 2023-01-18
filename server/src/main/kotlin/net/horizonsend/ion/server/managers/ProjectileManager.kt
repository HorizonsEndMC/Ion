package net.horizonsend.ion.server.managers

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.projectiles.RayTracedParticleProjectile

object ProjectileManager {
	private val rayTracedParticleProjectiles = mutableListOf<RayTracedParticleProjectile>()

	init {
		IonServer.Ion.server.scheduler.runTaskTimer(
			IonServer.Ion,
			Runnable {
				rayTracedParticleProjectiles.removeIf { projectile ->
					projectile.tick()
				}
			},
			0, 0
		)
	}

	fun addProjectile(rayTracedParticleProjectile: RayTracedParticleProjectile) {
		rayTracedParticleProjectiles.add(rayTracedParticleProjectile)
	}
}
