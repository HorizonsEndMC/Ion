package net.starlegacy.util

import org.bukkit.entity.Player
import java.lang.System.currentTimeMillis
import java.lang.System.nanoTime
import java.util.UUID
import java.util.concurrent.TimeUnit

class PerPlayerCooldown(cooldown: Long, timeUnit: TimeUnit = TimeUnit.MILLISECONDS) {
    private val map = mutableMapOf<UUID, Long>()

    private val cooldownNanos = timeUnit.toNanos(cooldown)

    fun tryExec(player: Player, block: () -> Unit) = tryExec(player, this.cooldownNanos, TimeUnit.NANOSECONDS, block)

    fun tryExec(player: Player, cooldown: Long, timeUnit: TimeUnit, block: () -> Unit) {
        if (nanoTime() - map.getOrElse(player.uniqueId) { 0 } >= timeUnit.toNanos(cooldown)) {
            map[player.uniqueId] = nanoTime()
            block()
        }
    }
}
