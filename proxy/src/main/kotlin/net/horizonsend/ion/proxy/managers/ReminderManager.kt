package net.horizonsend.ion.proxy.managers

import net.horizonsend.ion.proxy.PLUGIN
import java.util.concurrent.TimeUnit

object ReminderManager {
	private val scheduledMessages = listOf<Runnable>(
//		Runnable { voteReminder() }
	)

	private const val delay: Long = 900 // Might need to be increased if more messages are added (Currently 15 minutes)

	fun scheduleReminders() {
		 for (message in scheduledMessages) {
		 	PLUGIN.proxy.scheduler.schedule(
		 		PLUGIN,
		 		message,
		 		(delay / scheduledMessages.size.toLong()) * scheduledMessages.indexOf(message),
		 		delay,
		 		TimeUnit.SECONDS
		 	)
		 }
	}
// Might bring this back eventually
//	private fun voteReminder() = transaction {
//		for (player in PLUGIN.proxy.players) {
//			val playerData = PlayerData[player.uniqueId] ?: continue
//			val shouldPrompt: Boolean = playerData.voteTimes.find { it.dateTime.isBefore(LocalDateTime.now().minusDays(1)) } != null
//			if (shouldPrompt) player.special("Please vote for the server to help grow the community! <green><click:run_command:/vote>Run /vote to see where!")
//		}
//	}
}
