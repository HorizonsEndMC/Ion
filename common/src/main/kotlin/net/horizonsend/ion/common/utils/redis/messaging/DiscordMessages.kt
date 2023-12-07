package net.horizonsend.ion.common.utils.redis.messaging

import net.horizonsend.ion.common.IonComponent
import net.horizonsend.ion.common.utils.discord.Channel
import net.horizonsend.ion.common.utils.discord.Embed
import net.horizonsend.ion.common.utils.redis.RedisAction
import net.horizonsend.ion.common.utils.redis.RedisActions
import net.kyori.adventure.text.Component

abstract class DiscordMessages : IonComponent() {
	abstract val notifyAction: RedisAction<DiscordMessage>
	abstract val notifyEmbedAction: RedisAction<DiscordEmbedMessage>

	override fun onEnable() {
		RedisActions.register(notifyAction)
		RedisActions.register(notifyEmbedAction)
	}

	data class DiscordMessage(
		val channel: Channel,
		val message: Component
	)

	data class DiscordEmbedMessage(
		val channel: Channel,
		val embed: Embed
	)

	fun eventsMessage(message: Component) = notifyAction(DiscordMessage(Channel.EVENTS, message))
	fun globalMessage(message: Component) = notifyAction(DiscordMessage(Channel.GLOBAL, message))
	fun changelogMessage(message: Component) = notifyAction(DiscordMessage(Channel.CHANGELOG, message))

	fun changelogEmbed(message: Embed) = notifyEmbedAction(DiscordEmbedMessage(Channel.CHANGELOG, message))
	fun globalEmbed(message: Embed) = notifyEmbedAction(DiscordEmbedMessage(Channel.GLOBAL, message))
	fun eventsEmbed(message: Embed) = notifyEmbedAction(DiscordEmbedMessage(Channel.EVENTS, message))
}
