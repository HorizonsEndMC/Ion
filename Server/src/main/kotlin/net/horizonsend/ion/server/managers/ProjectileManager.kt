package net.horizonsend.ion.server.managers

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.projectiles.Projectile


object ProjectileManager {
	private val projectiles = mutableListOf<Projectile>()

	init {
		IonServer.Ion.server.scheduler.runTaskTimer(IonServer.Ion, Runnable {
			projectiles.removeIf { projectile ->
				projectile.tick()
				projectile.rayCastTick()
			}
		}, 0, 0)
	}

	fun addProjectile(projectile: Projectile) {
		projectiles.add(projectile)
	}
}