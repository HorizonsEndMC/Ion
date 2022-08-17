package net.horizonsend.ion.proxy.listeners.velocity

import com.velocitypowered.api.event.EventTask
import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import net.horizonsend.ion.common.database.PlayerData
import net.horizonsend.ion.proxy.annotations.VelocityListener
import net.horizonsend.ion.proxy.jda
import net.horizonsend.ion.proxy.proxyConfiguration
import org.jetbrains.exposed.sql.transactions.transaction

@VelocityListener
@Suppress("Unused")
class DisconnectListener {
	@Subscribe(order = PostOrder.LAST)
	fun onDisconnectEvent(event: DisconnectEvent): EventTask = EventTask.async {
		val memberId = transaction { PlayerData.findById(event.player.uniqueId)?.discordUUID } ?: return@async
		val guild = jda.getGuildById(proxyConfiguration.discordServer) ?: return@async

		guild.removeRoleFromMember(
			guild.getMemberById(memberId) ?: return@async,
			guild.getRoleById(proxyConfiguration.onlineRole) ?: return@async
		).queue()
	}
}