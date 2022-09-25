package net.horizonsend.ion.proxy.commands.discord

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Name
import co.aikar.commands.annotation.Subcommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.horizonsend.ion.common.database.collections.PlayerData
import net.horizonsend.ion.common.database.update
import net.horizonsend.ion.proxy.ProxyConfiguration
import net.horizonsend.ion.proxy.managers.LinkManager
import net.horizonsend.ion.proxy.messageEmbed

@Suppress("Unused")
@CommandAlias("account")
@Description("Manage the link between your Minecraft and Discord account.")
class DiscordAccountCommand(private val configuration: ProxyConfiguration) {
	@Subcommand("status")
	@Description("Check linked Minecraft account.")
	fun onStatusCommand(event: SlashCommandInteractionEvent) {
		val playerData = PlayerData[event.user.idLong]

		if (playerData?.discordId == null) {
			event.replyEmbeds(messageEmbed(description = "Your Discord account is not linked.", color = 0xff8844))
				.setEphemeral(true)
				.queue()
			return
		}

		event.replyEmbeds(messageEmbed(description = "Linked to ${playerData.minecraftUsername} (${playerData.minecraftUUID})."))
			.setEphemeral(true)
			.queue()
	}

	@Subcommand("unlink")
	@Description("Unlink Minecraft account.")
	fun onUnlinkCommand(event: SlashCommandInteractionEvent) {
		PlayerData[event.user.idLong]?.update {
			discordId = event.user.idLong
		}

		event.replyEmbeds(messageEmbed(description = "Your account is no longer linked, assuming it ever was."))
			.setEphemeral(true)
			.queue()
	}

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

		val playerData = PlayerData[playerUUID].update {
			discordId = event.user.idLong
		}

		event.replyEmbeds(
			messageEmbed(
				description = "Account linked to ${playerData.minecraftUsername}.",
				color = 0x00ff00
			)
		)
			.setEphemeral(true)
			.queue()

		event.jda.getGuildById(configuration.discordServer)!!.apply {
			addRoleToMember(event.user, getRoleById(configuration.linkedRole)!!).queue()
		}
	}
}