package net.horizonsend.ion.server.miscellaneous.utils.text

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.chat.Discord
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.kyori.adventure.text.Component

class PackagedMessage private constructor(val message: Component) {
	private var chat = false

	fun chat(): PackagedMessage {
		chat = true
		return this
	}

	private var crossServer = false

	fun crossServer(): PackagedMessage {
		crossServer = true
		return this
	}

	private var global = false

	fun global(): PackagedMessage {
		global = true
		return this
	}

	private var events = false

	fun events(): PackagedMessage {
		events = true
		return this
	}

	fun send() = send(message)

	fun send(message: Component) {
		if (chat && !crossServer) {
			IonServer.server.sendMessage(message)
		}

		if (crossServer && !chat) {
			Notify.notifyOnlineAction.invoke(message)
		}

		if (events) Discord.sendMessage(ConfigurationFiles.discordSettings().eventsChannel, message)
		if (global) Discord.sendMessage(ConfigurationFiles.discordSettings().globalChannel, message)
	}

	companion object {
		fun of(message: Component) = PackagedMessage(message)
	}
}
