package net.horizonsend.ion.proxy.commands.discord

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Name
import co.aikar.commands.annotation.Subcommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.horizonsend.ion.common.database.PlayerData
import net.horizonsend.ion.common.database.update
import net.horizonsend.ion.proxy.ProxyConfiguration
import net.horizonsend.ion.proxy.managers.LinkManager
import net.horizonsend.ion.proxy.messageEmbed

@CommandAlias("account")
@Description("Manage the link between your Minecraft and Discord account.")
class DiscordAccountCommand(private val configuration: ProxyConfiguration) {
	@Suppress("Unused")
	@Subcommand("status")
	@Description("Check linked Minecraft account.")
	fun onStatusCommand(event: SlashCommandInteractionEvent) {
		val playerData = PlayerData[event.user.idLong]

		if (playerData?.snowflake == null) {
			event.replyEmbeds(messageEmbed(description = "Your Discord account is not linked.", color = 0xff8844))
				.setEphemeral(true)
				.queue()
			return
		}

		event.replyEmbeds(messageEmbed(description = "Linked to ${playerData.username} (${playerData.uuid})."))
			.setEphemeral(true)
			.queue()
	}

	@Suppress("Unused")
	@Subcommand("unlink")
	@Description("Unlink Minecraft account.")
	fun onUnlinkCommand(event: SlashCommandInteractionEvent) {
		PlayerData[event.user.idLong]?.update {
			snowflake = null
		}

		event.replyEmbeds(messageEmbed(description = "Your account is no longer linked, assuming it ever was."))
			.setEphemeral(true)
			.queue()
	}

	@Suppress("Unused")
	@Subcommand("link")
	@Description("Link Minecraft account.")
	fun onLinkCommand(event: SlashCommandInteractionEvent, @Name("code") @Description("Link Code") code: String) {
		val playerUUID = LinkManager.validateLinkCode(code)

		if (playerUUID == null) {
			event.replyEmbeds(messageEmbed(description = "That link code is invalid or expired.", color = 0xff0000))
				.setEphemeral(true)
				.queue()
			return
		}

		val playerData = PlayerData[playerUUID]!!.update {
			snowflake = event.user.idLong
		}

		event.replyEmbeds(messageEmbed(description = "Account linked to $playerData.", color = 0x00ff00))
			.setEphemeral(true)
			.queue()

		event.jda.getGuildById(configuration.discordServer)!!.apply {
			addRoleToMember(event.user, getRoleById(configuration.linkedRole)!!).queue()
		}
	}
}
