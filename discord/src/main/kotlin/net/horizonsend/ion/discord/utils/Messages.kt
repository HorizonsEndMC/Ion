package net.horizonsend.ion.discord.utils

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.utils.data.DataObject
import net.horizonsend.ion.common.utils.discord.Channel
import net.horizonsend.ion.discord.IonDiscordBot

object Messages {
	private val guild = IonDiscordBot.server

	fun sendSimpleEmbed(channel: Channel, message: String) {
		sendEmbed(channel.getChannel(guild), messageEmbed(title = message))
	}

	fun sendFromJson(channel: Channel, serialized: String) {
		val data = DataObject.fromJson(serialized)
		val embed = EmbedBuilder.fromData(data).build()

		sendEmbed(channel.getChannel(guild), embed)
	}

	private fun sendEmbed(channel: TextChannel, embed: MessageEmbed, vararg others: MessageEmbed) {
		channel.sendMessageEmbeds(embed, *others).queue()
	}

	private fun sendText(channel: TextChannel, message: String) {
		channel.sendMessage(message).queue()
	}
}
