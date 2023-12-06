package net.horizonsend.ion.discord.features

//import net.horizonsend.ion.discord.features.redis.Messaging.getPlayers
import net.horizonsend.ion.common.IonComponent
import net.horizonsend.ion.discord.utils.IonDiscordScheduler

object PresenceManager : IonComponent() {
	override fun onEnable() {
		IonDiscordScheduler.asyncRepeat(0L, 1000L, ::updatePresence)
	}

	private fun updatePresence() {
//		IonDiscordBot.discord.presence.setPresence(OnlineStatus.ONLINE, Activity.playing("with ${getPlayers("proxy").count()} players!"))
	}
}
