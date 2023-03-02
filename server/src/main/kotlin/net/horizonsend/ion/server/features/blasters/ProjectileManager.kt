package net.horizonsend.ion.server.features.blasters

import net.horizonsend.ion.server.IonServer

object ProjectileManager {
	private val rayTracedParticleProjectiles = mutableListOf<RayTracedParticleProjectile>()

	init {
		IonServer.server.scheduler.runTaskTimer(
			IonServer,
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
