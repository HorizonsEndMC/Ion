package net.horizonsend.ion.proxy.listeners

import net.dv8tion.jda.api.JDA
import net.horizonsend.ion.common.database.collections.PlayerData
import net.horizonsend.ion.common.database.update
import net.horizonsend.ion.proxy.ProxyConfiguration
import net.md_5.bungee.api.event.LoginEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority

class LoginListener(private val configuration: ProxyConfiguration, private val jda: JDA) : Listener {
	@EventHandler(priority = EventPriority.HIGHEST)
	fun onLoginEvent(event: LoginEvent) {
		val memberId = PlayerData[event.connection.uniqueId]
			.update { minecraftUsername = event.connection.name }
			.discordId ?: return

		val guild = jda.getGuildById(configuration.discordServer) ?: return

		guild.addRoleToMember(
			guild.getMemberById(memberId) ?: return,
			guild.getRoleById(configuration.onlineRole) ?: return
		).queue()
	}
}