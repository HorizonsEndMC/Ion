package net.horizonsend.ion.discord.utils

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.MessageEmbed.AuthorInfo
import net.dv8tion.jda.api.entities.MessageEmbed.Field
import net.dv8tion.jda.api.entities.MessageEmbed.Footer
import net.horizonsend.ion.common.utils.discord.Embed
import java.time.Instant

fun Embed.jda(): MessageEmbed {
	val builder = EmbedBuilder()

	title?.let { builder.setTitle(it) }
	description?.let { builder.setDescription(it) }
	color?.let { builder.setColor(it) }
	fields?.let { for (field in it) builder.addField(field.jda()) }
	image?.let { builder.setImage(it) }
	thumbnail?.let { builder.setImage(it) }
	author?.let { builder.setAuthor(it.name, it.url, it.icon_url) }
	footer?.let { builder.setFooter(it.text, it.icon_url) }
	url?.let { builder.setUrl(url) }
	timestamp?.let { builder.setTimestamp(Instant.ofEpochMilli(it)) }

	return builder.build()
}

fun Embed.Field.jda(): Field {
	return Field(name, value, inline)
}

fun Embed.Author.jda(): AuthorInfo {
	return AuthorInfo(name, url, icon_url, proxy_icon_url)
}

fun Embed.Footer.jda(): Footer {
	return Footer(text, icon_url, proxy_icon_url)
}
