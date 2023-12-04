package net.horizonsend.ion.discord.command.commands

import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.schema.nations.Settlement
import net.horizonsend.ion.common.database.uuid
import net.horizonsend.ion.common.utils.miscellaneous.getDurationBreakdown
import net.horizonsend.ion.discord.command.InvalidCommandArgument
import net.horizonsend.ion.discord.command.IonDiscordCommand
import net.horizonsend.ion.discord.command.JDACommandManager
import net.horizonsend.ion.discord.command.annotations.CommandAlias
import net.horizonsend.ion.discord.command.annotations.Default
import net.horizonsend.ion.discord.command.annotations.Description
import net.horizonsend.ion.discord.command.annotations.ParamCompletion
import net.horizonsend.ion.discord.features.redis.Messaging.getPlayers
import net.horizonsend.ion.discord.utils.messageEmbed

@CommandAlias("playerinfo")
@Description("Get information about a player.")
object DiscordPlayerInfoCommand : IonDiscordCommand() {
	override fun onEnable(commandManager: JDACommandManager) {
		commandManager.registerCommandCompletion("onlinePlayers") { getPlayers("proxy").map { it.name } }
		commandManager.registerCommandCompletion("allPlayers") { SLPlayer.all().map { it.lastKnownName } }
	}

	@Default
	@Suppress("Unused")
	fun onPlayerInfo(
		event: SlashCommandInteractionEvent,
		@Description("Player's Name") @ParamCompletion("onlinePlayers") player: String
	) = asyncDiscordCommand(event) {
		val slPlayer = SLPlayer[player] ?: throw InvalidCommandArgument("Player $player not found!")

		val settlementId: Oid<Settlement>? = slPlayer.settlement
		val settlementInfo = settlementId?.let {
			val settlementName: String = Settlement.getName(settlementId) ?: throw Exception("Failed to get settlement data!")

			MessageEmbed.Field(
				"Settlement:",
				settlementName,
				true
			)
		}

		val nationId: Oid<Nation>? = slPlayer.nation
		val cachedNation = nationId?.let { NationCache[nationId] }

		val nationInfo = cachedNation?.let {
			val nationName = cachedNation.name

			MessageEmbed.Field(
				"Nation:",
				nationName,
				true
			)
		}

		val xpField = MessageEmbed.Field(
			"XP:",
			slPlayer.xp.toString(),
			true
		)
		val levelField = MessageEmbed.Field(
			"Level:",
			slPlayer.level.toString(),
			true
		)
		val bountyField = MessageEmbed.Field(
			"Bounty:",
			slPlayer.bounty.toString(),
			true
		)

		val time: Long = System.currentTimeMillis() - slPlayer.lastSeen.time
		val prefix: String = when {
			getPlayers("proxy").any { it.uniqueId == slPlayer._id.uuid }  -> "Online"
			else -> "Offline"
		}
		val onlineField = MessageEmbed.Field(
			"Last Seen:",
			"$prefix for ${getDurationBreakdown(time)}",
			false
		)

		val fields = listOfNotNull(
			settlementInfo,
			nationInfo,
			xpField,
			levelField,
			bountyField,
			onlineField
		)

		respondEmbed(
			event,
			messageEmbed(
				title = "Player: $player",
				fields = fields,
				thumbnail = MessageEmbed.Thumbnail("https://minotar.net/avatar/$player", null, 16, 16),
				color = cachedNation?.color ?: Integer.parseInt("ffffff", 16)
			)
		)
	}
}
