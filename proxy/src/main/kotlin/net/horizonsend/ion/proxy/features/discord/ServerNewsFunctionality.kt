package net.horizonsend.ion.proxy.features.discord

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed.Field
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.horizonsend.ion.common.utils.discord.Embed
import net.horizonsend.ion.proxy.PLUGIN

class ServerNewsFunctionality(val jda: JDA) : ListenerAdapter() {
	init {
	    PLUGIN.logger.info("Registered Server News Listener")
	}

	private val configuration = PLUGIN.discordConfiguration

	override fun onMessageReceived(event: MessageReceivedEvent) {
		if (event.channel.idLong != configuration.serverNewsChannel) return
		if (event.author.isBot) return

		sendMessageForApproval(event.message)

		event.author.openPrivateChannel()
			.flatMap { privateChannel ->
				privateChannel.sendMessageEmbeds(Embed(
					title = "This Channel Requires Message Approval",
					description = "When approved, your message will appear."
				).jda())
			}
			.queue()

		event.message
			.delete()
			.queue()
	}

	private fun sendMessageForApproval(message: Message) {
		val embed = EmbedBuilder()
			.setTitle("Message awaiting approval")
			.addField(Field("Channel", message.channel.name, false))
			.addField(Field("Sender", message.author.name, false))
			.addField(Field("Sender ID", message.author.id, false))
			.build()

		val channel = jda.getTextChannelById(configuration.approvalQueueChannel) ?: return


		val approve: Button = Button.success("approve", "Approve")
		val deny: Button = Button.danger("deny", "Deny")

		channel
			.sendMessage(message.contentRaw)
			.setEmbeds(embed)
			.setActionRow(listOf(approve, deny))
			.queue()
	}

	override fun onButtonInteraction(event: ButtonInteractionEvent) {
		event.deferReply(true)

		val newsChannel = jda.getTextChannelById(configuration.serverNewsChannel)
		if (newsChannel == null) {
			event.replyEmbeds(EmbedBuilder().setTitle("News channel not configured!").build()).setEphemeral(true).queue()
			return
		}

		if (event.button.id == "approve") {
			val authorPing = event.message.embeds.firstNotNullOfOrNull { it.fields.firstOrNull { field -> field.name == "Sender ID" } }?.let { it.value?.toLongOrNull() }

			if (authorPing == null) {
				event.replyEmbeds(EmbedBuilder().setTitle("Improper User ID!").build()).setEphemeral(true).queue()
				return
			}

			val authorUser = jda.getUserById(authorPing)
			if (authorUser == null) {
				event.replyEmbeds(EmbedBuilder().setTitle("User not found!").build()).setEphemeral(true).queue()
				return
			}

			newsChannel.sendMessageEmbeds(EmbedBuilder().setDescription(event.message.contentRaw).setAuthor(authorUser.effectiveName, authorUser.avatarUrl).build()).queue()

			event.replyEmbeds(EmbedBuilder().setTitle("Message Approved").build()).setEphemeral(true).queue()
			return
		}

		if (event.button.id == "deny") {
			event.replyEmbeds(EmbedBuilder().setTitle("Message Denied").build()).setEphemeral(true).queue()
		}
	}
}
