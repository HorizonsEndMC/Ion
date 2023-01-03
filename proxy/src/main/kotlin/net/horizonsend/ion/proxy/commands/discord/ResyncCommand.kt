package net.horizonsend.ion.proxy.commands.discord

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Description
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.horizonsend.ion.proxy.ProxyConfiguration
import net.horizonsend.ion.proxy.managers.SyncManager
import net.horizonsend.ion.proxy.messageEmbed

@CommandAlias("resync")
@Description("Resync all roles")
class ResyncCommand(private val jda: JDA, private val configuration: ProxyConfiguration) {
	@Default
	@Suppress("Unused")
	fun onResyncCommand(event: SlashCommandInteractionEvent) {
		if (event.user.idLong != 521031433972744193 || event.user.idLong != 152566944925483009) {
			event.replyEmbeds(messageEmbed(title = "You do not have permission to use this command.", color = 0xff8844))
				.setEphemeral(true)
				.queue()
			return
		}

		event.deferReply(true).queue()

		val changeLog = SyncManager(jda, configuration).sync()

		if (changeLog.isEmpty()) {
			event.hook.editOriginalEmbeds(
				messageEmbed(
					title = "Done - No changes were made.",
					color = 0x7fff7f
				)
			).queue()
		} else {
			event.hook.editOriginalEmbeds(
				messageEmbed(
					title = "Done, the following changes were made:",
					description = changeLog.joinToString("\n", "", ""),
					color = 0x7fff7f
				)
			).queue()
		}
	}
}
