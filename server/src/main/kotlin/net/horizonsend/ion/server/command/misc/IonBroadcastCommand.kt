package net.horizonsend.ion.server.command.misc

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.utils.discord.Embed
import net.horizonsend.ion.common.utils.text.subStringBetween
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.chat.Discord
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage

@CommandAlias("ionbroadcast")
@CommandPermission("ion.broadcast")
object IonBroadcastCommand : SLCommand() {
	@Subcommand("global chat")
    fun onNotifyGlobal(message: String) {
		Notify.chatAndGlobal(miniMessage().deserialize(message))
	}

	@Subcommand("global embed")
    fun onNotifyGlobalEmbed(message: String) {
		val embed = processString(message)

		Discord.sendEmbed(ConfigurationFiles.discordSettings().globalChannel, embed)
	}

	@Subcommand("events embed")
    fun onNotifyEventsEmbed(message: String) {
		val embed = processString(message)

		Discord.sendEmbed(ConfigurationFiles.discordSettings().eventsChannel, embed)
	}

	@Subcommand("events message")
    fun onNotifyEventsMessage(message: String) {
		Discord.sendMessage(ConfigurationFiles.discordSettings().eventsChannel, message)
	}

	/**
	 * Converts an input string to an embed
	 *
	 * Not supported:
	   * fields
	   * author
	   * footer
	 **/
	private fun processString(message: String): Embed = Embed(
		title = getValue("title", message),
		description = getValue("description", message),
		color = getValue("color", message)?.toInt(),
		image = getValue("image", message),
		thumbnail = getValue("thumbnail", message),
		url = getValue("url", message),
		timestamp = getValue("timestamp", message)?.toLong()
	)

	fun getValue(field: String, message: String): String? {
		if (!message.contains(field)) return null

		return message.subStringBetween("$field=\"", "\"")
	}
}
