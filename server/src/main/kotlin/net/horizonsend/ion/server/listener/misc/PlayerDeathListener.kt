package net.horizonsend.ion.server.listener.misc

import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.listener.SLEventListener
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.kyori.adventure.text.Component
import org.bukkit.damage.DamageType
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.PlayerDeathEvent

object PlayerDeathListener : SLEventListener() {
	@EventHandler(ignoreCancelled = false, priority = EventPriority.MONITOR)
	fun onPlayerDeath(event: PlayerDeathEvent) {
		if (event.isCancelled) return

		if (!ConfigurationFiles.serverConfiguration().crossServerDeathMessages) return

		val message = event.deathMessage()
		message?.let {
			Notify.chatAndGlobal(message)
			event.deathMessage(null)
		}
	}

	@EventHandler
	fun onPlayerDeathAboveMaxHeight(event: PlayerDeathEvent) {
		if (event.isCancelled) return

		if (event.damageSource.damageType != DamageType.OUT_OF_WORLD) return

		val player = event.player
		if (player.location.y < player.location.world.maxHeight + 64) return

		event.deathMessage(Component.text("${player.name} ascended above the world"))
	}
}
