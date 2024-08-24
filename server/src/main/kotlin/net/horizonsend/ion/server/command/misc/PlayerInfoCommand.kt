package net.horizonsend.ion.server.command.misc

import co.aikar.commands.ConditionFailedException
import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Default
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.cache.nations.RelationCache
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.schema.nations.NationRelation
import net.horizonsend.ion.common.database.schema.nations.Settlement
import net.horizonsend.ion.common.database.uuid
import net.horizonsend.ion.common.utils.miscellaneous.getDurationBreakdown
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.player.NewPlayerProtection.hasProtection
import net.horizonsend.ion.server.miscellaneous.utils.get
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
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
		sendInfo(sender, slPlayer)
	}

	@Suppress("Unused")
	@Default
	@CommandAlias("playerinfo|pinfo|pi")
	fun onExecute(sender: Player) = asyncCommand(sender) {
		val slPlayer = SLPlayer[sender]
		sendInfo(sender, slPlayer)
	}

	private fun sendInfo(sender: CommandSender, target: SLPlayer) {
		sender.sendRichMessage("<yellow>Player <gold>${target.lastKnownName}")

		sendNationsInfo(sender, target)

		sendAdvanceInfo(sender, target)

		sendGracePeriodInfo(sender, target)

		sendBountyInfo(sender, target)

		sender.sendRichMessage("<gray>Last Seen: ${getInactiveTimeText(target)}")
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
							val relation: NationRelation.Level = RelationCache[nationId, senderNation]

							sender.sendMessage(
								text()
									.append(text("Relation: ", NamedTextColor.GRAY))
									.append(relation.component)
									.build()
							)
						}
					}
				}
			}
		}
	}

	private fun sendAdvanceInfo(sender: CommandSender, slPlayer: SLPlayer) {
		sender.sendRichMessage("<dark_purple>XP: <light_purple>${slPlayer.xp}")
		sender.sendRichMessage("<red>Level: <yellow>${slPlayer.level}")
	}

	private fun sendGracePeriodInfo(sender: CommandSender, slPlayer: SLPlayer) {
		if (Bukkit.getPlayer(slPlayer._id.uuid)?.hasProtection() != false) {
			sender.sendRichMessage("<yellow>Grace Period: <gold>True")
		}
	}

	private fun sendBountyInfo(sender: CommandSender, player: SLPlayer) {
		if (player.bounty > 0.0) {
			sender.sendRichMessage("<gold>Bounty: <red>${player.bounty}")
		}
	}

	private fun getInactiveTimeText(player: SLPlayer): String {
		val time: Long = System.currentTimeMillis() - player.lastSeen.time

		val prefix: String = when {
			Bukkit.getPlayer(player._id.uuid) != null -> "<green>Online"
			else -> "<red>Offline"
		}

		return "$prefix<gray> for ${getDurationBreakdown(time)}"
	}

	override fun supportsVanilla(): Boolean {
		return true
	}
}
