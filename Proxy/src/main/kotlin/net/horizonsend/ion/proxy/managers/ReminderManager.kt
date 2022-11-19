package net.horizonsend.ion.proxy.managers

import java.util.concurrent.TimeUnit
import net.horizonsend.ion.proxy.IonProxy.Companion.Ion
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder

object ReminderManager {

	fun reschedule() {
		Ion.proxy.scheduler.schedule(Ion, { reminderManager() }, 10, 10, TimeUnit.SECONDS)
	}

	private fun reminderManager() {
		val message = ComponentBuilder()
			.append(
				ComponentBuilder("Please vote for our server and Help us grow the Horizon's End community!\nDo /Vote to see where you can.")
					.color(ChatColor.GOLD)
					.underlined(true)
					.create()
			)
			.create()

		Ion.proxy.broadcast(*message)

		reschedule()
	}
}