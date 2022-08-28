package net.horizonsend.ion.proxy.listeners.bungee

import net.dv8tion.jda.api.JDA
import net.horizonsend.ion.common.database.PlayerData
import net.horizonsend.ion.proxy.ProxyConfiguration
import net.md_5.bungee.api.event.LoginEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority
import org.jetbrains.exposed.sql.transactions.transaction

@Suppress("Unused")
class LoginListener(private val configuration: ProxyConfiguration, private val jda: JDA?) : Listener {
	@EventHandler(priority = EventPriority.HIGHEST)
	fun onLoginEvent(event: LoginEvent) {
		jda?.let{
			val memberId = transaction {
				PlayerData.getOrCreate(event.connection.uniqueId, event.connection.name).discordUUID
			} ?: return
			val guild = jda.getGuildById(configuration.discordServer) ?: return

			guild.addRoleToMember(
				guild.getMemberById(memberId) ?: return,
				guild.getRoleById(configuration.onlineRole) ?: return
			).queue()
		}
	}
}