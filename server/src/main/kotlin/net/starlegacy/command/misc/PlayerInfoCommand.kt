package net.starlegacy.command.misc

import co.aikar.commands.ConditionFailedException
import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Default
import net.starlegacy.command.SLCommand
import net.starlegacy.database.Oid
import net.starlegacy.database.schema.misc.SLPlayer
import net.starlegacy.database.schema.nations.Nation
import net.starlegacy.database.schema.nations.NationRelation
import net.starlegacy.database.schema.nations.Settlement
import net.starlegacy.database.uuid
import net.starlegacy.util.getDurationBreakdown
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object PlayerInfoCommand : SLCommand() {
	@Suppress("Unused")
	@Default
	@CommandAlias("playerinfo|pinfo|pi")
	@CommandCompletion("@players")
	fun onExecute(sender: CommandSender, player: String) = asyncCommand(sender) {
		val slPlayer = SLPlayer[player] ?: throw InvalidCommandArgument("Player $player not found!")

		sender.sendRichMessage("<yellow>Player <gold>${slPlayer.lastKnownName}")

		sendNationsInfo(sender, slPlayer)

		sendAdvanceInfo(sender, slPlayer)

		sender.sendRichMessage("<gray>Last Seen: ${getInactiveTimeText(slPlayer)}")
	}

	private fun sendNationsInfo(sender: CommandSender, slPlayer: SLPlayer) {
		val settlementId: Oid<Settlement>? = slPlayer.settlement

		if (settlementId != null) {
			val settlementName: String = Settlement.getName(settlementId)
				?: throw ConditionFailedException("Failed to get settlement data!")

			sender.sendRichMessage("<dark_aqua>Settlement: <aqua>$settlementName")

			val nationId = slPlayer.nation

			if (nationId != null) {
				val nationName: String = Nation.findPropById(nationId, Nation::name)!!
				sender.sendRichMessage("<dark_green>Nation: <green>$nationName")

				if (sender is Player) {
					val senderSettlement: Oid<Settlement>? = SLPlayer[sender].settlement

					if (senderSettlement != null) {
						val senderNation: Oid<Nation>? = Settlement.findPropById(senderSettlement, Settlement::nation)

						if (senderNation != null) {
							val relation: NationRelation.Level = NationRelation.getRelationActual(nationId, senderNation)

							sender.sendRichMessage("<gray>Relation: ${relation.coloredName}")
						}
					}
				}
			}
		}
	}

	private fun sendAdvanceInfo(sender: CommandSender, slPlayer: SLPlayer) {
		sender.sendRichMessage("<dark_purple>SLXP: <light_purple>${slPlayer.xp}")
		sender.sendRichMessage("<red>Level: <yellow>${slPlayer.level}")
	}

	private fun getInactiveTimeText(player: SLPlayer): String {
		val time: Long = System.currentTimeMillis() - player.lastSeen.time

		val prefix: String = when {
			Bukkit.getPlayer(player._id.uuid) != null -> "<green>Online"
			else -> "<red>Offline"
		}

		return "$prefix$<gray> for ${getDurationBreakdown(time)}"
	}

	override fun supportsVanilla(): Boolean {
		return true
	}
}
