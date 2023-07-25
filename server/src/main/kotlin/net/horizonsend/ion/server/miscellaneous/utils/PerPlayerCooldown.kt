package net.horizonsend.ion.server.miscellaneous.utils

import net.horizonsend.ion.server.command.admin.debug
import org.bukkit.entity.Player
import java.lang.System.nanoTime
import java.util.UUID
import java.util.concurrent.TimeUnit

class PerPlayerCooldown(cooldown: Long, timeUnit: TimeUnit = TimeUnit.MILLISECONDS) {
	private val map = mutableMapOf<UUID, Long>()

	private val cooldownNanos = timeUnit.toNanos(cooldown)

	fun tryExec(player: Player, block: () -> Unit) = tryExec(player, this.cooldownNanos, TimeUnit.NANOSECONDS, block)

	fun tryExec(player: Player, cooldown: Long, timeUnit: TimeUnit, block: () -> Unit) {
		player.debug("cooldown check")
		if (nanoTime() - map.getOrElse(player.uniqueId) { 0 } >= timeUnit.toNanos(cooldown)) {
			player.debug("not cooled down, going on")
			map[player.uniqueId] = nanoTime()
			block()
		}
	}
}
