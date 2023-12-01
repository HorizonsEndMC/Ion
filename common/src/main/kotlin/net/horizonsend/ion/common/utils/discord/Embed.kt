package net.horizonsend.ion.common.utils.discord

import kotlinx.serialization.Serializable

@Serializable
data class Message(
	val embeds: ArrayList<Embed>
)

@Serializable
data class Embed(
	val type: String = "rich",
	val title: String? = null,
	val description: String? = null,
	val color: String? = null,
	val fields: ArrayList<Field>? = null,
	val image: Image? = null,
	val thumbnail: Image? = null,
	val author: Author? = null,
	val footer: Footer? = null,
	val url: String? = null
) {
	@Serializable
	data class Field(
		val name: String,
		val value: String,
		val inline: Boolean = false,
	)

	@Serializable
	data class Image (
		val url: String,
		val proxy_url: String,
		val height: Int,
		val width: Int
	)

	@Serializable
	data class Author(
		val name: String,
		val url: String,
		val icon_url: String,
		val proxy_icon_url: String,
	)

	@Serializable
	data class Footer(
		val text: String,
		val icon_url: String,
		val proxy_icon_url: String,
	)
}
