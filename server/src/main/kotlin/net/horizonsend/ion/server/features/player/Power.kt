package net.horizonsend.ion.server.features.player

import net.horizonsend.ion.server.core.IonServerComponent
import net.horizonsend.ion.server.features.progression.SLXP
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.PlayerDeathEvent
import java.util.concurrent.TimeUnit

object Power : IonServerComponent() {
	override fun onEnable() {
		// award power to online players every 30 minutes
		// 20 ticks * 30 minutes converted to seconds
		Tasks.syncRepeat(0L, 20L * TimeUnit.MINUTES.toSeconds(15)) {
			for (player in Bukkit.getOnlinePlayers()) {
				SLXP.addPowerAsync(player.uniqueId, 2)
			}
		}
	}

	@EventHandler
	fun modifyPowerOnPlayerDeath(event: PlayerDeathEvent) {
		val victim = event.player
		val killer = event.entity.killer ?: return // only player vs. player kills should modify power

		SLXP.addPowerAsync(victim.uniqueId, -4)
		SLXP.addPowerAsync(killer.uniqueId, 4)
	}
}
