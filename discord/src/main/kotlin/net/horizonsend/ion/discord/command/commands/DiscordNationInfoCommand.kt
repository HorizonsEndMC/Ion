package net.horizonsend.ion.discord.command.commands

import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.common.database.cache.nations.SettlementCache
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.schema.nations.Territory
import net.horizonsend.ion.common.utils.miscellaneous.toCreditsString
import net.horizonsend.ion.discord.command.IonDiscordCommand
import net.horizonsend.ion.discord.command.JDACommandManager
import net.horizonsend.ion.discord.command.annotations.CommandAlias
import net.horizonsend.ion.discord.command.annotations.Default
import net.horizonsend.ion.discord.command.annotations.Description
import net.horizonsend.ion.discord.command.annotations.ParamCompletion
import net.horizonsend.ion.discord.utils.messageEmbed
import org.litote.kmongo.eq
import java.util.Date

@CommandAlias("nationinfo")
@Description("Get information about a nation.")
object DiscordNationInfoCommand : IonDiscordCommand() {
	override fun onEnable(commandManager: JDACommandManager) {
		commandManager.registerCommandCompletion("nations") { NationCache.all().map { it.name } }
	}

	@Default
	@Suppress("Unused")
	fun onNationInfo(
		event: SlashCommandInteractionEvent,
		@Description("Player's Name") @ParamCompletion("nations") name: String
	) = asyncDiscordCommand(event) {
		val nationId = resolveNation(name)
		val nation = Nation.findById(nationId) ?: fail { "Failed to load data" }
		val cached = NationCache[nationId]

		val outposts = Territory.findProps(Territory::nation eq nationId, Territory::name)
			.map { it[Territory::name] }

		val outpostsField = if (outposts.count() > 0)
			MessageEmbed.Field(
				"Outposts (${outposts.count()})",
				outposts.joinToString(),
				false
		) else null

		val settlements: List<SettlementCache.SettlementData> = Nation.getSettlements(nationId)
			.sortedByDescending { SLPlayer.count(SLPlayer::settlement eq it) }
			.toList()
			.map { SettlementCache[it] }

		val settlementsField = MessageEmbed.Field(
			"Settlements: (${settlements.size})",
			settlements.joinToString { it.name },
			false
		)

		val leaderField = MessageEmbed.Field(
			"Leader:",
			getPlayerName(cached.leader),
			false
		)

		val balanceField = MessageEmbed.Field(
			"Balance:",
			nation.balance.toCreditsString(),
			false
		)

		val members: List<Pair<String, Date>> = SLPlayer
			.findProps(SLPlayer::nation eq nationId, SLPlayer::lastKnownName, SLPlayer::lastSeen)
			.map { it[SLPlayer::lastKnownName] to it[SLPlayer::lastSeen] }
			.sortedByDescending { it.second }

		val playersField = MessageEmbed.Field(
			"Players (${members.size})",
			members.joinToString { it.first },
			false
		)

		val fields: List<MessageEmbed.Field> = listOfNotNull(
			outpostsField,
			settlementsField,
			balanceField,
			leaderField,
			playersField
		)

		respondEmbed(
			event,
			messageEmbed(
				title = "Nation: $name",
				fields = fields,
				color = nation.color
			)
		)
	}
}
