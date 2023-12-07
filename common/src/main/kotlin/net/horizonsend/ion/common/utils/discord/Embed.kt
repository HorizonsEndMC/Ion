package net.horizonsend.ion.common.utils.discord

data class Message(
	val embeds: ArrayList<Embed>
)

data class Embed(
	val type: String = "rich",
	val title: String? = null,
	val description: String? = null,
	val color: Int? = null,
	val fields: List<Field>? = null,
	val image: String? = null,
	val thumbnail: String? = null,
	val author: Author? = null,
	val footer: Footer? = null,
	val url: String? = null,
	val timestamp: Long? = null,
) {
	data class Field(
		val name: String,
		val value: String,
		val inline: Boolean = false,
	)

	data class Author(
		val name: String,
		val url: String,
		val icon_url: String,
		val proxy_icon_url: String,
	)

	data class Footer(
		val text: String,
		val icon_url: String,
		val proxy_icon_url: String,
	)
}
