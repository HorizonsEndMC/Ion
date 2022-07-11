package net.horizonsend.ion.proxy.utilities

import java.time.OffsetDateTime
import net.dv8tion.jda.api.entities.EmbedType
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.MessageEmbed.AuthorInfo
import net.dv8tion.jda.api.entities.MessageEmbed.Field
import net.dv8tion.jda.api.entities.MessageEmbed.Footer
import net.dv8tion.jda.api.entities.MessageEmbed.ImageInfo
import net.dv8tion.jda.api.entities.MessageEmbed.Provider
import net.dv8tion.jda.api.entities.MessageEmbed.Thumbnail
import net.dv8tion.jda.api.entities.MessageEmbed.VideoInfo

/**
 * Util Function because I am fed up of putting null a dozen times.
 */
@Suppress("Nothing_To_Inline")
inline fun messageEmbed(
	url: String? = null, title: String? = null, description: String? = null, type: EmbedType? = EmbedType.RICH,
	timestamp: OffsetDateTime? = null, color: Int = 0xff7f3f, thumbnail: Thumbnail? = null,
	siteProvider: Provider? = null, author: AuthorInfo? = null, videoInfo: VideoInfo? = null, footer: Footer? = null,
	image: ImageInfo? = null, fields: List<Field>? = null
) = MessageEmbed(
	url, title, description, type, timestamp, color, thumbnail, siteProvider, author, videoInfo, footer, image, fields
)