package net.horizonsend.ion.proxy.commands.waterfall

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Subcommand
import net.dv8tion.jda.api.JDA
import net.horizonsend.ion.common.database.PlayerData
import net.horizonsend.ion.proxy.ProxyConfiguration
import net.horizonsend.ion.proxy.managers.LinkManager
import net.horizonsend.ion.proxy.managers.SyncManager
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.connection.ProxiedPlayer

@CommandAlias("account")
@Description("Manage the link between your Minecraft and Discord account.")
class BungeeAccountCommand(private val jda: JDA, private val configuration: ProxyConfiguration) : BaseCommand() {
	@Suppress("Unused")
	@Subcommand("status")
	@Description("Check linked Discord account.")
	fun onStatusCommand(sender: ProxiedPlayer) {
		val playerData = PlayerData[sender.uniqueId]

		if (playerData?.snowflake == null) {
			sender.sendMessage(
				*ComponentBuilder("Your Minecraft account is not linked.")
					.color(ChatColor.of("#8888ff"))
					.create()
			)
			return
		}

		jda.retrieveUserById(playerData.snowflake!!).queue {
			sender.sendMessage(
				*ComponentBuilder()
					.append(
						ComponentBuilder("Linked to ")
							.color(ChatColor.of("#8888ff"))
							.create()
					)
					.append(
						ComponentBuilder("\"${it.asTag}\"")
							.color(ChatColor.WHITE)
							.create()
					)
					.append(
						ComponentBuilder(" (")
							.color(ChatColor.of("#8888ff"))
							.create()
					)
					.append(
						ComponentBuilder("\"${playerData.snowflake!!}\"")
							.color(ChatColor.WHITE)
							.create()
					)
					.append(
						ComponentBuilder(").")
							.color(ChatColor.of("#8888ff"))
							.create()
					)
					.create()
			)
		}
	}

	@Suppress("Unused")
	@Subcommand("unlink")
	@Description("Unlink Discord account.")
	fun onUnlinkCommand(sender: ProxiedPlayer) {
		val playerData = PlayerData[sender.uniqueId]

		if (playerData?.snowflake == null) {
			sender.sendMessage(
				*ComponentBuilder("Your account is not linked.")
					.color(ChatColor.of("#ff8844"))
					.create()
			)
			return
		}

		jda.getGuildById(configuration.discordServer)!!.apply {
			getMemberById(playerData.snowflake!!)?.let { member ->
				removeRoleFromMember(member, getRoleById(configuration.linkedRole)!!).queue()
			}
		}

		playerData.update {
			snowflake = null
		}

		sender.sendMessage(
			*ComponentBuilder("Your account is no longer linked.")
				.color(ChatColor.of("#88ff88"))
				.create()
		)
	}

	@Suppress("Unused")
	@Subcommand("link")
	@Description("Link Discord account.")
	fun onLinkCommand(sender: ProxiedPlayer) = sender.sendMessage(
		*ComponentBuilder()
			.append(
				ComponentBuilder("Run /account link ")
					.color(ChatColor.of("#8888ff"))
					.create()
			)
			.append(
				ComponentBuilder("\"${LinkManager.createLinkCode(sender.uniqueId)}\"")
					.color(ChatColor.WHITE)
					.create()
			)
			.append(
				ComponentBuilder(" in Discord to link your accounts. The code will expire in 5 minutes.\n")
					.color(ChatColor.of("#8888ff"))
					.create()
			)
			.append(
				ComponentBuilder("Please note, this is a slash command, not a text command, make sure Discord recognises it as such before sending otherwise it will be automatically deleted.")
					.color(ChatColor.of("#3f3f3f"))
					.italic(true)
					.create()
			)
			.create()
	)

	@Suppress("unused")
	@CommandPermission("ion.resync")
	@Subcommand("resync")
	@Description("Sync discord roles")
	fun onReSync() {
		SyncManager(jda, configuration).sync()
	}
}
