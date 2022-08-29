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

		event.deferReply(true).queue()

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

		val changeLog = mutableListOf<String>()

		val membersWithLinked = guild.getMembersWithRoles(linkedRole)
		val membersWithUnlinked = guild.getMembersWithRoles(unlinkedRole)

		for (member in guild.members) {
			val playerData = PlayerData.find { PlayerDataTable.discordUUID eq member.idLong }.firstOrNull()

			if (playerData == null) {
				if (membersWithLinked.contains(member)) {
					guild.removeRoleFromMember(member, linkedRole).queue()
					changeLog += "Removed ${linkedRole.asMention} from ${member.asMention}"
				}
				if (!membersWithUnlinked.contains(member)) {
					guild.addRoleToMember(member, unlinkedRole).queue()
					changeLog += "Added ${unlinkedRole.asMention} from ${member.asMention}"
				}
			} else {
				if (!membersWithLinked.contains(member)) {
					guild.addRoleToMember(member, linkedRole).queue()
					changeLog += "Added ${linkedRole.asMention} from ${member.asMention}"
				}
				if (membersWithUnlinked.contains(member)) {
					guild.removeRoleFromMember(member, unlinkedRole).queue()
					changeLog += "Removed ${unlinkedRole.asMention} from ${member.asMention}"
				}
			}
		}

		event.hook.editOriginalEmbeds(messageEmbed(
			title = changeLog.joinToString("\n", "Done, the following changes were made:", ""),
			color = 0x7fff7f
		)).queue()
	}
}