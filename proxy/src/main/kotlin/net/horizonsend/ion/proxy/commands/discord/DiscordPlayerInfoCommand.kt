package net.horizonsend.ion.proxy.commands.discord

import co.aikar.commands.ConditionFailedException
import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Description
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.schema.nations.Settlement
import net.horizonsend.ion.common.database.uuid
import net.horizonsend.ion.common.utils.miscellaneous.getDurationBreakdown
import net.horizonsend.ion.proxy.JDACommandManager
import net.horizonsend.ion.proxy.PLUGIN
import net.horizonsend.ion.proxy.messageEmbed

@CommandAlias("playerinfo")
@Description("Get information about a player.")
object DiscordPlayerInfoCommand : IonDiscordCommand() {
	override fun onEnable(commandManager: JDACommandManager) {
		commandManager.registerCommandCompletion("players") { SLPlayer.all().map { it.lastKnownName } }
	}

	@Default
	@Suppress("Unused")

	fun onPlayerInfo(
		event: SlashCommandInteractionEvent,
		@Description("Player's Name") @ParamCompletion("players") player: String
	) = asyncDiscordCommand(event) {
		val slPlayer = SLPlayer[player] ?: throw InvalidCommandArgument("Player $player not found!")

		val settlementId: Oid<Settlement>? = slPlayer.settlement
		val settlementInfo = settlementId?.let {
			val settlementName: String = Settlement.getName(settlementId) ?: throw ConditionFailedException("Failed to get settlement data!")

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

		val time: Long = System.currentTimeMillis() - slPlayer.lastSeen.time
		val prefix: String = when {
			PLUGIN.getProxy().getPlayer(slPlayer._id.uuid) != null -> "Online"
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
