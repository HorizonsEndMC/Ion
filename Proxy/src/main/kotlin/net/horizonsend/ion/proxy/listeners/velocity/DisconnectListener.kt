package net.horizonsend.ion.proxy.listeners.velocity

import com.velocitypowered.api.event.EventTask
import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import net.dv8tion.jda.api.JDA
import net.horizonsend.ion.common.database.PlayerData
import net.horizonsend.ion.proxy.ProxyConfiguration
import net.horizonsend.ion.proxy.annotations.VelocityListener
import org.jetbrains.exposed.sql.transactions.transaction

@VelocityListener
@Suppress("Unused")
class DisconnectListener(private val jda: JDA, private val configuration: ProxyConfiguration) {
	@Subscribe(order = PostOrder.LAST)
	fun onDisconnectEvent(event: DisconnectEvent): EventTask = EventTask.async {
		val memberId = transaction { PlayerData.findById(event.player.uniqueId)?.discordUUID } ?: return@async
		val guild = jda.getGuildById(configuration.discordServer) ?: return@async

		guild.removeRoleFromMember(
			guild.getMemberById(memberId) ?: return@async,
			guild.getRoleById(configuration.onlineRole) ?: return@async
		).queue()
	}
}