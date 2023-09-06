package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.features.starship.Damager
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.Projectiles
import net.horizonsend.ion.server.miscellaneous.utils.Tasks

abstract class Projectile(val starship: ActiveStarship?, val shooter: Damager) {
	open fun fire() {
		Tasks.syncDelay(0, ::tick)
	}

	protected abstract fun tick()

	protected fun reschedule() {
		Tasks.syncDelay(Projectiles.TICK_INTERVAL, ::tick)
	}
}
