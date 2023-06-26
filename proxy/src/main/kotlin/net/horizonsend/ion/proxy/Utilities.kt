package net.horizonsend.ion.proxy

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandManager
import com.velocitypowered.api.command.CommandSource
import net.dv8tion.jda.api.entities.EmbedType
import net.dv8tion.jda.api.entities.MessageEmbed
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.minimessage.MiniMessage
import java.time.OffsetDateTime

fun Audience.sendRichMessage(s: String) = sendMessage(
	MiniMessage.miniMessage().deserialize(s)
)

fun CommandManager.register(vararg messageCommand: List<LiteralArgumentBuilder<CommandSource>>) {
	messageCommand.forEach { it.forEach { this.register(BrigadierCommand(it)) } }
}

@Suppress("Nothing_To_Inline")
inline fun messageEmbed(
	url: String? = null,
	title: String? = null,
	description: String? = null,
	type: EmbedType? = EmbedType.RICH,
	timestamp: OffsetDateTime? = null,
	color: Int = 0xff7f3f,
	thumbnail: MessageEmbed.Thumbnail? = null,
	siteProvider: MessageEmbed.Provider? = null,
	author: MessageEmbed.AuthorInfo? = null,
	videoInfo: MessageEmbed.VideoInfo? = null,
	footer: MessageEmbed.Footer? = null,
	image: MessageEmbed.ImageInfo? = null,
	fields: List<MessageEmbed.Field>? = null
) = MessageEmbed(
	url, title, description, type, timestamp, color, thumbnail, siteProvider, author, videoInfo, footer, image, fields
)
