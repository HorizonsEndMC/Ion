package net.horizonsend.ion.server.miscellaneous.utils

import dev.vankka.mcdiscordreserializer.discord.DiscordSerializer
import dev.vankka.mcdiscordreserializer.discord.DiscordSerializerOptions
import github.scarsz.discordsrv.DiscordSRV
import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder
import github.scarsz.discordsrv.dependencies.jda.api.JDA
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel
import net.horizonsend.ion.common.utils.discord.Embed
import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.server.IonServerComponent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.KeybindComponent
import net.kyori.adventure.translation.GlobalTranslator
import org.bukkit.Bukkit.getPluginManager
import java.time.Instant
import java.util.Locale

object Discord : IonServerComponent(true) {
	private val serializer = DiscordSerializer(DiscordSerializerOptions(
		true,
		false,
		KeybindComponent::keybind
	) { component -> GlobalTranslator.render(component, Locale.ENGLISH).plainText() })

	private var enabled: Boolean = false
	private var JDA: JDA? = null

	override fun onEnable() {
		if (!getPluginManager().isPluginEnabled("DiscordSRV")) return

		val jda = DiscordSRV.getPlugin().jda

		if (jda == null) {
			log.warn("Discord JDA null!")
			return
		}

		JDA = jda
		enabled = true
	}

	private val channelMap: MutableMap<Long, TextChannel?> = mutableMapOf()
	private fun getChannel(id: Long): TextChannel? {
		if (!enabled) return null

		channelMap[id]?.let { return it }

		val channel = JDA?.getTextChannelById(id)
		channelMap[id] = channel

		return channel
	}

	fun asDiscord(component: Component): String = serializer.serialize(component)

	fun sendEmbed(channel: Long, embed: Embed, vararg embeds: Embed) {
		if (!enabled) return

		val textChannel = getChannel(channel) ?: return

		textChannel.sendMessageEmbeds(embed.jda(), *embeds.map { it.jda() }.toTypedArray()).queue()
	}

	fun sendMessage(channel: Long, message: String) {
		if (!enabled) return
		val textChannel = getChannel(channel) ?: return

		textChannel.sendMessage(message).queue()
	}

	fun sendMessage(channel: Long, message: Component) {
		if (!enabled) return
		val textChannel = getChannel(channel) ?: return

		textChannel.sendMessage(asDiscord(message)).queue()
	}
}

fun Embed.jda(): MessageEmbed {
	val builder = EmbedBuilder()

	title?.let { builder.setTitle(it) }
	description?.let { builder.setDescription(it) }
	color?.let { builder.setColor(it) }
	fields?.let { for (field in it) builder.addField(field.jda()) }
	image?.let { builder.setImage(it) }
	thumbnail?.let { builder.setThumbnail(it) }
	author?.let { builder.setAuthor(it.name, it.url, it.icon_url) }
	footer?.let { builder.setFooter(it.text, it.icon_url) }
//	url?.let { builder.setUrl(url) }
	timestamp?.let { builder.setTimestamp(Instant.ofEpochMilli(it)) }

	return builder.build()
}

fun Embed.Field.jda(): MessageEmbed.Field {
	return MessageEmbed.Field(name, value, inline)
}
