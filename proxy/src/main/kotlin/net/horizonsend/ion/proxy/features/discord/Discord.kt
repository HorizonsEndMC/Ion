package net.horizonsend.ion.proxy.features.discord

import dev.vankka.mcdiscordreserializer.discord.DiscordSerializer
import dev.vankka.mcdiscordreserializer.discord.DiscordSerializerOptions
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity.playing
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.ChunkingFilter
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.api.utils.cache.CacheFlag
import net.horizonsend.ion.common.utils.discord.Embed
import net.horizonsend.ion.proxy.IonProxyComponent
import net.horizonsend.ion.proxy.PLUGIN
import net.horizonsend.ion.proxy.commands.discord.DiscordInfoCommand
import net.horizonsend.ion.proxy.commands.discord.DiscordNationCommand
import net.horizonsend.ion.proxy.commands.discord.DiscordPlayerCommand
import net.horizonsend.ion.proxy.commands.discord.DiscordPlayerListCommand
import net.horizonsend.ion.proxy.commands.discord.DiscordSettlementCommand
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.KeybindComponent
import net.kyori.adventure.text.TranslatableComponent
import java.time.Instant
import java.util.concurrent.TimeUnit

object Discord : IonProxyComponent() {
	private val serializer = DiscordSerializer(DiscordSerializerOptions(
		true,
		false,
		KeybindComponent::keybind,
		TranslatableComponent::key
	))

	private val configuration get() = PLUGIN.discordConfiguration
	private var enabled: Boolean = false
	private var JDA: JDA? = null
	var commandManager: SlashCommandManager? = null; private set

	override fun onEnable() = PLUGIN.proxy.scheduler.async {
		try {
			val jda = JDABuilder.createLight(configuration.token)
				.setEnabledIntents(GatewayIntent.GUILD_MEMBERS)
				.setMemberCachePolicy(MemberCachePolicy.ALL)
				.setChunkingFilter(ChunkingFilter.ALL)
				.disableCache(CacheFlag.entries)
				.setEnableShutdownHook(false)
				.build()
				.awaitReady()

			JDA = jda

			val commandManager = SlashCommandManager(jda, configuration)
			this.commandManager = commandManager

			commandManager.registerGlobalCommand(DiscordPlayerListCommand)
			commandManager.registerGlobalCommand(DiscordPlayerCommand)
			commandManager.registerGlobalCommand(DiscordNationCommand)
			commandManager.registerGlobalCommand(DiscordSettlementCommand)
			commandManager.registerGlobalCommand(DiscordInfoCommand)

			commandManager.build()

			enabled = true

			PLUGIN.proxy.scheduler.repeat(5, 5, TimeUnit.SECONDS, Discord::updateBotPresence)
		} catch (e: Throwable) {
			log.error("Failed to start JDA! $e")

			e.printStackTrace()
		}
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

	private fun updateBotPresence() {
		if (!enabled) return

		JDA?.presence?.setPresence(
			OnlineStatus.ONLINE,
			playing("with ${PLUGIN.proxy.onlineCount} players!")
		)
	}
}

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

fun Embed.Field.jda(): MessageEmbed.Field {
	return MessageEmbed.Field(name, value, inline)
}

fun Embed.Author.jda(): MessageEmbed.AuthorInfo {
	return MessageEmbed.AuthorInfo(name, url, icon_url, proxy_icon_url)
}

fun Embed.Footer.jda(): MessageEmbed.Footer {
	return MessageEmbed.Footer(text, icon_url, proxy_icon_url)
}
