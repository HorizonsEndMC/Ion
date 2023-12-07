package net.horizonsend.ion.discord.features

import com.google.gson.reflect.TypeToken
import net.horizonsend.ion.common.utils.redis.RedisAction
import net.horizonsend.ion.common.utils.redis.messaging.DiscordMessages
import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.discord.IonDiscordBot
import net.horizonsend.ion.discord.utils.getChannel
import net.horizonsend.ion.discord.utils.jda

object DiscordMessages : DiscordMessages() {
	override val notifyAction: RedisAction<DiscordMessage> = object : RedisAction<DiscordMessage>(
		"notify-discord",
		object : TypeToken<DiscordMessage>() {}.type,
		false
	) {
		override fun onReceive(data: DiscordMessage) {
			val (channel, component) = data

			val textChannel = channel.getChannel(IonDiscordBot.server)
			textChannel.sendMessage(component.plainText()).queue()
		}
	}

	override val notifyEmbedAction: RedisAction<DiscordEmbedMessage> = object : RedisAction<DiscordEmbedMessage>(
		"notify-discord-embed",
		object : TypeToken<DiscordEmbedMessage>() {}.type,
		false
	) {
		override fun onReceive(data: DiscordEmbedMessage) {
			val (channel, embed) = data

			val textChannel = channel.getChannel(IonDiscordBot.server)
			textChannel.sendMessageEmbeds(embed.jda()).queue()
		}
	}
}
