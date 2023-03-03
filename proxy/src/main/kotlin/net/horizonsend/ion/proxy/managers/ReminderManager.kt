package net.horizonsend.ion.proxy.managers

import net.horizonsend.ion.common.database.PlayerData
import net.horizonsend.ion.proxy.IonProxy.Companion.Ion
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT
import net.md_5.bungee.api.chat.hover.content.Text
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

object ReminderManager {
	private val scheduledMessages = listOf(
		Runnable { voteReminder() }
	)

	private const val delay: Long = 900 // Might need to be increased if more messages are added (Currently 15 minutes)

	fun scheduleReminders() {
		for (message in scheduledMessages) {
			Ion.proxy.scheduler.schedule(
				Ion,
				message,
				(delay / scheduledMessages.size.toLong()) * scheduledMessages.indexOf(message),
				delay,
				TimeUnit.SECONDS
			)
		}
	}

	private fun voteReminder() {
		val message = ComponentBuilder()
			.append(
				ComponentBuilder("Please vote for our server to help us grow the Horizon's End community!\n")
					.color(ChatColor.GOLD)
					.bold(true)
					.create()
			)
			.append(
				ComponentBuilder("Do /Vote to see where you can.")
					.bold(false)
					.color(ChatColor.GREEN)
					.event(ClickEvent(RUN_COMMAND, "/vote"))
					.event(HoverEvent(SHOW_TEXT, Text("/Vote")))
					.create()
			)

		for (player in Ion.proxy.players) {
			val playerData = PlayerData[player.uniqueId]!!
			val shouldPrompt: Boolean = playerData.voteTimes.find { it.dateTime.isBefore(LocalDateTime.now().minusDays(1)) } != null
			if (shouldPrompt) player.sendMessage(*message.create())
		}
	}
}
