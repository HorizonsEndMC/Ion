package net.horizonsend.ion.discord.utils

import net.dv8tion.jda.api.entities.EmbedType
import net.dv8tion.jda.api.entities.MessageEmbed
import java.time.OffsetDateTime

@Suppress("Nothing_To_Inline")
inline fun messageEmbed(
	url: String? = null,
	title: String? = null,
	description: String? = null,
	type: EmbedType? = EmbedType.RICH,
	timestamp: OffsetDateTime? = null,
	color: Int = 0xF2AE67,
	thumbnail: MessageEmbed.Thumbnail? = null,
	siteProvider: MessageEmbed.Provider? = null,
	author: MessageEmbed.AuthorInfo? = null,
	videoInfo: MessageEmbed.VideoInfo? = null,
	footer: MessageEmbed.Footer? = null,
	image: MessageEmbed.ImageInfo? = null,
	fields: List<MessageEmbed.Field>? = null
) = MessageEmbed(url, title, description, type, timestamp, color, thumbnail, siteProvider, author, videoInfo, footer, image, fields)
