package net.horizonsend.ion.proxy.commands.discord

import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.common.database.cache.nations.SettlementCache
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.nations.Settlement
import net.horizonsend.ion.common.database.schema.nations.Territory
import net.horizonsend.ion.common.utils.miscellaneous.toCreditsString
import net.horizonsend.ion.proxy.features.discord.DiscordCommand
import net.horizonsend.ion.proxy.features.discord.DiscordSubcommand.Companion.subcommand
import net.horizonsend.ion.proxy.features.discord.ExecutableCommand
import net.horizonsend.ion.proxy.features.discord.SlashCommandManager
import net.horizonsend.ion.proxy.utils.messageEmbed
import org.litote.kmongo.eq
import java.util.Date

object DiscordSettlementCommand : DiscordCommand("settlement", "Settlement commands") {
	override fun setup(commandManager: SlashCommandManager) {
		commandManager.registerCompletion("settlements") { SettlementCache.all().map { it.name } }

		registerSubcommand(onInfo)
	}

	private	val onInfo = subcommand(
		"info",
		"Get information about a settlement",
		listOf(ExecutableCommand.CommandField("settlement", OptionType.STRING, "settlements", "The name of the settlement"))
	) { event ->
		val name = event.getOption("settlement")?.asString ?: fail { "You must enter a settlement name!" }

		val settlementId = resolveSettlement(name)
		val settlement = Settlement.findById(settlementId) ?: fail { "Failed to load data" }
		val nation = settlement.nation?.let { NationCache[it] }

		val nationField = nation?.let {
			MessageEmbed.Field(
				"Nation: ",
				it.name,
				false
			)
		}

		val territory = Territory.findOne(Territory::settlement eq settlementId) ?: fail { "Settlement territory not found! Please contact an admin." }

		val outpostsField = MessageEmbed.Field(
			"Territory:",
			territory.name,
			false
		)

		val leaderField = MessageEmbed.Field(
			"Leader:",
			getPlayerName(settlement.leader),
			false
		)

		val balanceField = MessageEmbed.Field(
			"Balance:",
			settlement.balance.toCreditsString(),
			false
		)

		val members: List<Pair<String, Date>> = SLPlayer
			.findProps(SLPlayer::settlement eq settlementId, SLPlayer::lastKnownName, SLPlayer::lastSeen)
			.map { it[SLPlayer::lastKnownName] to it[SLPlayer::lastSeen] }
			.sortedByDescending { it.second }

		val playersField = MessageEmbed.Field(
			"Players (${members.size})",
			members.joinToString { it.first },
			false
		)

		val fields: List<MessageEmbed.Field> = listOfNotNull(
			nationField,
			outpostsField,
			balanceField,
			leaderField,
			playersField
		)

		respondEmbed(
			event,
			messageEmbed(
				title = "Settlement: $name",
				fields = fields,
				color = nation?.color ?: Integer.parseInt("ffffff", 16)
			)
		)
	}
}
