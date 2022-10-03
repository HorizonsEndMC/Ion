package net.horizonsend.ion.proxy.listeners

import net.horizonsend.ion.common.database.collections.PlayerData
import net.horizonsend.ion.proxy.IonProxy
import net.md_5.bungee.api.event.PlayerDisconnectEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority

class PlayerDisconnectListener : Listener {
	@EventHandler(priority = EventPriority.HIGHEST)
	fun onPlayerDisconnectEvent(event: PlayerDisconnectEvent) {
		val discordId = PlayerData[event.player.uniqueId].discordId ?: return
		val guild = IonProxy.jda!!.getGuildById(IonProxy.configuration.discordServer) ?: return

		guild.removeRoleFromMember(
			guild.getMemberById(discordId) ?: return,
			guild.getRoleById(IonProxy.configuration.onlineRole) ?: return
		).queue()
	}
}