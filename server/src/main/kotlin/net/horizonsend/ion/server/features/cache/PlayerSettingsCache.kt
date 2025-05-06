package net.horizonsend.ion.server.features.cache

import net.horizonsend.ion.common.database.cache.AbstractPlayerSettingsCache
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.slPlayerId
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.listen
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.event.EventPriority
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

object PlayerSettingsCache : AbstractPlayerSettingsCache() {
	override fun kickId(player: SLPlayerId, reason: Component) {
		Bukkit.getPlayer(player.id)?.kick(reason)
	}

	override fun runAsync(task: () -> Unit) {
		Tasks.async(task)
	}

	override fun load() {
		listen<PlayerQuitEvent> { event ->
			callOnQuit(event.player.slPlayerId)
		}

		listen<AsyncPlayerPreLoginEvent>(priority = EventPriority.MONITOR, ignoreCancelled = true) { event ->
			callOnPreLogin(event.uniqueId.slPlayerId)
		}

		listen<PlayerJoinEvent>(priority = EventPriority.LOWEST) { event ->
			callOnLoginLow(event.player.slPlayerId)
		}
	}
}
