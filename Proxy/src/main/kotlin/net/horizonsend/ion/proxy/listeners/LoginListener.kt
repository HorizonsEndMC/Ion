package net.horizonsend.ion.proxy.listeners

import net.horizonsend.ion.common.database.collections.PlayerData
import net.horizonsend.ion.common.database.update
import net.horizonsend.ion.proxy.IonProxy
import net.md_5.bungee.api.event.LoginEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority

class LoginListener : Listener {
	@EventHandler(priority = EventPriority.HIGHEST)
	fun onLoginEvent(event: LoginEvent) {
		val playerData = PlayerData[event.connection.uniqueId]

		if (playerData.minecraftUsername != event.connection.name) {
			playerData.update { minecraftUsername = event.connection.name }
		}

		IonProxy.jda?.let { jda ->
			val discordId = playerData.discordId ?: return

			val guild = jda.getGuildById(IonProxy.configuration.discordServer) ?: return

			guild.addRoleToMember(
				guild.getMemberById(discordId) ?: return,
				guild.getRoleById(IonProxy.configuration.onlineRole) ?: return
			).queue()
		}
	}
}