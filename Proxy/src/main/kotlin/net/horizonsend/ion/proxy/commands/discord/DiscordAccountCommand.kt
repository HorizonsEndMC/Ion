package net.horizonsend.ion.proxy.commands.discord

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.horizonsend.ion.common.annotations.CommandMeta
import net.horizonsend.ion.common.database.PlayerData
import net.horizonsend.ion.common.database.PlayerDataTable
import net.horizonsend.ion.proxy.managers.LinkManager
import net.horizonsend.ion.proxy.proxyConfiguration
import net.horizonsend.ion.proxy.utilities.messageEmbed
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

@CommandMeta("account", "Manage the link between your Minecraft and Discord account.")
class DiscordAccountCommand {
	@Suppress("Unused")
	@CommandMeta("status", "Check linked Minecraft account.")
	fun onStatusCommand(event: SlashCommandInteractionEvent) {
		val playerData = transaction { PlayerData.find(PlayerDataTable.discordUUID eq event.user.idLong).firstOrNull() }

		if (playerData?.discordUUID == null) {
			event.replyEmbeds(messageEmbed(description = "Your Discord account is not linked.", color = 0xff8844))
				.setEphemeral(true)
				.queue()
			return
		}

		event.replyEmbeds(messageEmbed(description = "Linked to ${playerData.mcUsername} (${playerData.id.value})."))
			.setEphemeral(true)
			.queue()
	}

	@Suppress("Unused")
	@CommandMeta("unlink", "Unlink Minecraft account.")
	fun onUnlinkCommand(event: SlashCommandInteractionEvent) {
		transaction {
			PlayerData.find(PlayerDataTable.discordUUID eq event.user.idLong).firstOrNull()?.discordUUID = null
		}
		event.replyEmbeds(messageEmbed(description = "Your account is no longer linked, assuming it ever was."))
			.setEphemeral(true)
			.queue()
	}

	@Suppress("Unused")
	@CommandMeta("link", "Link Minecraft account.")
	fun onLinkCommand(event: SlashCommandInteractionEvent, @CommandMeta("code", "Link Code") code: String) {
		val playerUUID = LinkManager.validateLinkCode(code)

		if (playerUUID == null) {
			event.replyEmbeds(messageEmbed(description = "That link code is invalid or expired.", color = 0xff0000))
				.setEphemeral(true)
				.queue()
			return
		}

		transaction {
			val playerData = PlayerData.findById(playerUUID)

			playerData?.discordUUID = event.user.idLong

			event.replyEmbeds(
				messageEmbed(
					description = "Account linked to ${playerData?.mcUsername}.",
					color = 0x00ff00
				)
			)
				.setEphemeral(true)
				.queue()
		}

		event.jda.getGuildById(proxyConfiguration.discordServer)!!.apply {
			addRoleToMember(event.user, getRoleById(proxyConfiguration.linkedRole)!!).queue()
		}
	}

	@Suppress("Unused")
	@CommandMeta("update", "Force update your roles on Discord.")
	fun onUpdateCommand(event: SlashCommandInteractionEvent) = transaction {
		event.jda.getGuildById(proxyConfiguration.discordServer)!!.apply {
			getRoleById(proxyConfiguration.linkedRole)!!.let {
				val playerData = PlayerData.find(PlayerDataTable.discordUUID eq event.user.idLong).firstOrNull()

				if (playerData?.discordUUID == null) removeRoleFromMember(
					event.user,
					it
				).queue() else addRoleToMember(event.user, it).queue()
			}
		}

		event.replyEmbeds(messageEmbed(description = "Roles updated.")).setEphemeral(true).queue()
	}
}