package net.horizonsend.ion.server.managers

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.projectiles.Projectile

object ProjectileManager {
	private val projectiles = mutableListOf<Projectile>()

	init {
		IonServer.plugin.server.scheduler.runTaskTimerAsynchronously(IonServer.plugin, Runnable {
			projectiles.removeIf { projectile ->
				projectile.tick()
			}
		}, 0, 0)
	}

	fun addProjectile(projectile: Projectile) {
		projectiles.add(projectile)
	}
}