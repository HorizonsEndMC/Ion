package net.starlegacy.feature.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.features.starship.controllers.Controller
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.subsystem.weapon.Projectiles
import net.starlegacy.util.Tasks
import org.bukkit.entity.Player

abstract class Projectile(val starship: ActiveStarship?, val shooter: Controller?) {
	open fun fire() {
		Tasks.syncDelay(0, ::tick)
	}

	protected abstract fun tick()

	protected fun reschedule() {
		Tasks.syncDelay(Projectiles.TICK_INTERVAL, ::tick)
	}
}
