package net.horizonsend.ion.proxy.commands.discord

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Description
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.common.database.cache.nations.SettlementCache
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.nations.Settlement
import net.horizonsend.ion.common.database.schema.nations.Territory
import net.horizonsend.ion.common.utils.miscellaneous.toCreditsString
import net.horizonsend.ion.proxy.JDACommandManager
import net.horizonsend.ion.proxy.messageEmbed
import org.litote.kmongo.eq
import java.util.Date

@CommandAlias("settlementinfo")
@Description("Get information about a settlement.")
object DiscordSettlementInfoCommand : IonDiscordCommand() {
	override fun onEnable(commandManager: JDACommandManager) {
		commandManager.registerCommandCompletion("settlements") { SettlementCache.all().map { it.name } }
	}

	@Default
	@Suppress("Unused")
	fun onSettlementInfo(
		event: SlashCommandInteractionEvent,
		@Description("Settlement's Name") @ParamCompletion("settlements") name: String
	) = asyncDiscordCommand(event) {
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
