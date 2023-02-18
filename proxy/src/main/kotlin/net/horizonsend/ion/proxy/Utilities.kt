package net.horizonsend.ion.proxy

import net.dv8tion.jda.api.entities.EmbedType
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.MessageEmbed.AuthorInfo
import net.dv8tion.jda.api.entities.MessageEmbed.Field
import net.dv8tion.jda.api.entities.MessageEmbed.Footer
import net.dv8tion.jda.api.entities.MessageEmbed.ImageInfo
import net.dv8tion.jda.api.entities.MessageEmbed.Provider
import net.dv8tion.jda.api.entities.MessageEmbed.Thumbnail
import net.dv8tion.jda.api.entities.MessageEmbed.VideoInfo
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer
import net.md_5.bungee.api.CommandSender
import java.time.OffsetDateTime

fun CommandSender.sendRichMessage(s: String) = sendMessage(
	*BungeeComponentSerializer.get().serialize(
		MiniMessage.miniMessage().deserialize(s)
	)
)

/**
 * Utility function for creating JDA MessageEmbed's without specifying null a bunch of time.
 * @see MessageEmbed
 */
@Suppress("Nothing_To_Inline")
inline fun messageEmbed(
	url: String? = null,
	title: String? = null,
	description: String? = null,
	type: EmbedType? = EmbedType.RICH,
	timestamp: OffsetDateTime? = null,
	color: Int = 0xff7f3f,
	thumbnail: Thumbnail? = null,
	siteProvider: Provider? = null,
	author: AuthorInfo? = null,
	videoInfo: VideoInfo? = null,
	footer: Footer? = null,
	image: ImageInfo? = null,
	fields: List<Field>? = null
) = MessageEmbed(
	url, title, description, type, timestamp, color, thumbnail, siteProvider, author, videoInfo, footer, image, fields
)
