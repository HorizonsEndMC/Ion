package net.horizonsend.ion.proxy.commands.bungee

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Subcommand
import net.dv8tion.jda.api.JDA
import net.horizonsend.ion.common.database.sql.PlayerData
import net.horizonsend.ion.proxy.ProxyConfiguration
import net.horizonsend.ion.proxy.managers.LinkManager
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.connection.ProxiedPlayer
import org.jetbrains.exposed.sql.transactions.transaction

@Suppress("Unused")
@CommandAlias("account")
@Description("Manage the link between your Minecraft and Discord account.")
class BungeeAccountCommand(private val jda: JDA, private val configuration: ProxyConfiguration) : BaseCommand() {
	@Subcommand("status")
	@Description("Check linked Discord account.")
	fun onStatusCommand(sender: ProxiedPlayer) {
		val playerData = transaction { PlayerData.findById(sender.uniqueId) }

		if (playerData?.discordUUID == null) {
			sender.sendMessage(
				*ComponentBuilder("Your Minecraft account is not linked.")
					.color(ChatColor.of("#8888ff"))
					.create()
			)
			return
		}

		jda.retrieveUserById(playerData.discordUUID!!).queue {
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
							.create())
					.append(
						ComponentBuilder(" (")
							.color(ChatColor.of("#8888ff"))
							.create())
					.append(
						ComponentBuilder("\"${playerData.discordUUID!!}\"")
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

	@Subcommand("unlink")
	@Description("Unlink Discord account.")
	fun onUnlinkCommand(sender: ProxiedPlayer) = transaction {
		val playerData = PlayerData.findById(sender.uniqueId)

		if (playerData?.discordUUID == null) {
			sender.sendMessage(
				*ComponentBuilder("Your account is not linked.")
					.color(ChatColor.of("#ff8844"))
					.create()
			)
			return@transaction
		}

		jda.getGuildById(configuration.discordServer)!!.apply {
			getMemberById(playerData.discordUUID!!)?.let { member ->
				removeRoleFromMember(member, getRoleById(configuration.linkedRole)!!).queue()
			}
		}

		playerData.discordUUID = null

		sender.sendMessage(
			*ComponentBuilder("Your account is no longer linked.")
				.color(ChatColor.of("#88ff88"))
				.create()
		)
	}

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
				ComponentBuilder(" in Discord to link your accounts. The code will expire in 5 minutes.")
					.color(ChatColor.of("#8888ff"))
					.create()
			)
			.create()
	)
}