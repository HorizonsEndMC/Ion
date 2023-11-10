package net.horizonsend.ion.server.miscellaneous.utils

import net.horizonsend.ion.server.features.starship.control.controllers.Controller
import net.horizonsend.ion.server.features.starship.damager.Damager
import org.bukkit.entity.Player
import java.lang.System.nanoTime
import java.util.UUID
import java.util.concurrent.TimeUnit

open class AbstractCooldown <T> (cooldown: Long, timeUnit: TimeUnit = TimeUnit.MILLISECONDS) {
	private val map = mutableMapOf<T, Long>()

	val cooldownNanos = timeUnit.toNanos(cooldown)

	fun tryExec(player: T, block: () -> Unit) = tryExec(player, this.cooldownNanos, TimeUnit.NANOSECONDS, block)

	fun tryExec(player: T, cooldown: Long, timeUnit: TimeUnit, block: () -> Unit) {
		if (nanoTime() - map.getOrElse(player) { 0 } >= timeUnit.toNanos(cooldown)) {
			map[player] = nanoTime()
			block()
		} else cooldownRejected(player)
	}

	open fun cooldownRejected(player: T) {}
}

open class PerPlayerCooldown(cooldown: Long, timeUnit: TimeUnit = TimeUnit.MILLISECONDS): AbstractCooldown<UUID>(cooldown, timeUnit) {
	fun tryExec(player: Player, block: () -> Unit) = tryExec(player.uniqueId, this.cooldownNanos, TimeUnit.NANOSECONDS, block)
}

class PerControllerCooldown(cooldown: Long, timeUnit: TimeUnit = TimeUnit.MILLISECONDS): AbstractCooldown<Controller>(cooldown, timeUnit)
class PerDamagerCooldown(cooldown: Long, timeUnit: TimeUnit = TimeUnit.MILLISECONDS): AbstractCooldown<Damager>(cooldown, timeUnit)

