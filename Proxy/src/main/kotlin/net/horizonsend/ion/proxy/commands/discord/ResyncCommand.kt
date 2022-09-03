package net.horizonsend.ion.proxy.commands.discord

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Description
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.horizonsend.ion.common.database.PlayerData
import net.horizonsend.ion.common.database.PlayerDataTable
import net.horizonsend.ion.proxy.ProxyConfiguration
import net.horizonsend.ion.proxy.annotations.GuildCommand
import net.horizonsend.ion.proxy.messageEmbed
import org.jetbrains.exposed.sql.transactions.transaction

@GuildCommand
@Suppress("Unused")
@CommandAlias("resync")
@Description("Resync all roles")
class ResyncCommand(private val configuration: ProxyConfiguration) {
	@Default
	fun onResyncCommand(event: SlashCommandInteractionEvent) {
		if (event.user.idLong != 521031433972744193) {
			event.replyEmbeds(messageEmbed(title = "You do not have permission to use this command.", color = 0xff8844))
				.setEphemeral(true)
				.queue()
			return
		}

		val guild = event.jda.getGuildById(configuration.discordServer) ?: run {
			event.hook.editOriginalEmbeds(messageEmbed(title = "Guild is not set.", color = 0xff8844)).queue()
			return
		}

		val linkedRole = guild.getRoleById(configuration.linkedRole) ?: run {
			event.hook.editOriginalEmbeds(messageEmbed(title = "Guild linked role is not set.", color = 0xff8844)).queue()
			return
		}

		val unlinkedRole = guild.getRoleById(configuration.unlinkedRole) ?: run {
			event.hook.editOriginalEmbeds(messageEmbed(title = "Guild unlinked role is not set.", color = 0xff8844)).queue()
			return
		}

		val linkBypassRole = guild.getRoleById(configuration.linkBypassRole) ?: run {
			event.hook.editOriginalEmbeds(messageEmbed(title = "Guild link bypass role is not set.", color = 0xff8844)).queue()
			return
		}

		event.deferReply(true).queue()

		val changeLog = mutableListOf<String>()

		val membersWithLinked = guild.getMembersWithRoles(linkedRole)
		val membersWithUnlinked = guild.getMembersWithRoles(unlinkedRole)
		val membersWithLinkBypass = guild.getMembersWithRoles(linkBypassRole)

		for (member in guild.members) {
			val playerData = transaction { PlayerData.find { PlayerDataTable.discordUUID eq member.idLong }.firstOrNull() }

			val isOverridden = member.user.isBot || membersWithLinkBypass.contains(member)

			val shouldHaveUnlinked = !isOverridden && playerData == null
			val shouldHaveLinked = !isOverridden && playerData != null

			if (shouldHaveLinked && !membersWithLinked.contains(member)) {
				guild.addRoleToMember(member, linkedRole).queue()
				changeLog += "- Granted ${linkedRole.asMention} to ${member.asMention}"
			}

			if (!shouldHaveLinked && membersWithLinked.contains(member)) {
				guild.removeRoleFromMember(member, linkedRole).queue()
				changeLog += "- Removed ${linkedRole.asMention} from ${member.asMention}"
			}

			if (shouldHaveUnlinked && !membersWithUnlinked.contains(member)) {
				guild.addRoleToMember(member, unlinkedRole).queue()
				changeLog += "- Granted ${unlinkedRole.asMention} to ${member.asMention}"
			}

			if (!shouldHaveLinked && membersWithLinked.contains(member)) {
				guild.removeRoleFromMember(member, unlinkedRole).queue()
				changeLog += "- Removed ${unlinkedRole.asMention} from ${member.asMention}"
			}
		}

		if (changeLog.isEmpty()) {
			event.hook.editOriginalEmbeds(messageEmbed(
				title = "Done - No changes were made.",
				color = 0x7fff7f
			)).queue()
		} else {
			event.hook.editOriginalEmbeds(messageEmbed(
				title = "Done, the following changes were made:",
				description = changeLog.joinToString("\n", "", ""),
				color = 0x7fff7f
			)).queue()
		}
	}
}