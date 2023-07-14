package net.horizonsend.ion.proxy.utils

import net.dv8tion.jda.api.entities.EmbedType
import net.dv8tion.jda.api.entities.MessageEmbed
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.minimessage.MiniMessage
import java.time.OffsetDateTime

fun Audience.sendRichMessage(s: String) = sendMessage(
	MiniMessage.miniMessage().deserialize(s)
)

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
