package net.horizonsend.ion.proxy.listeners.bungee

import net.dv8tion.jda.api.JDA
import net.horizonsend.ion.common.database.PlayerData
import net.horizonsend.ion.proxy.ProxyConfiguration
import net.md_5.bungee.api.event.PlayerDisconnectEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority
import org.jetbrains.exposed.sql.transactions.transaction

@Suppress("Unused")
class PlayerDisconnectListener(private val jda: JDA, private val configuration: ProxyConfiguration) : Listener {
	@EventHandler(priority = EventPriority.HIGHEST)
	fun onPlayerDisconnectEvent(event: PlayerDisconnectEvent) {
		val memberId = transaction { PlayerData.findById(event.player.uniqueId)?.discordUUID } ?: return
		val guild = jda.getGuildById(configuration.discordServer) ?: return

		guild.removeRoleFromMember(
			guild.getMemberById(memberId) ?: return,
			guild.getRoleById(configuration.onlineRole) ?: return
		).queue()
	}
}