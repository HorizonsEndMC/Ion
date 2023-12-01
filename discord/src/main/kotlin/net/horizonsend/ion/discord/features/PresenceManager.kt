package net.horizonsend.ion.discord.features

import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.horizonsend.ion.common.IonComponent
import net.horizonsend.ion.discord.IonDiscordBot
import net.horizonsend.ion.discord.utils.IonDiscordScheduler
import net.horizonsend.ion.discord.utils.redis.Messaging.getPlayers

object PresenceManager : IonComponent() {
	override fun onEnable() {
		IonDiscordScheduler.asyncRepeat(0L, 1000L, ::updatePresence)
	}

	private fun updatePresence() {
		IonDiscordBot.discord.presence.setPresence(OnlineStatus.ONLINE, Activity.playing("with ${getPlayers("proxy").count()} players!"))
	}
}
