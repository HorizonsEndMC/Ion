package net.starlegacy.command.progression

import co.aikar.commands.ConditionFailedException
import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Optional
import java.util.UUID
import net.md_5.bungee.api.chat.TextComponent
import net.starlegacy.command.SLCommand
import net.starlegacy.database.schema.misc.SLPlayer
import net.starlegacy.database.slPlayerId
import net.starlegacy.feature.progression.Levels
import net.starlegacy.feature.progression.MAX_LEVEL
import net.starlegacy.util.aqua
import net.starlegacy.util.darkAqua
import net.starlegacy.util.darkPurple
import net.starlegacy.util.gray
import net.starlegacy.util.lightPurple
import net.starlegacy.util.msg
import net.starlegacy.util.plus
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object XPCommand : SLCommand() {
	@CommandAlias("slxp|xp")
	fun execute(sender: CommandSender, @Optional player: String?) = asyncCommand(sender) {
		val playerID: UUID = player?.let { resolveOfflinePlayer(it) }
			?: (sender as? Player)?.uniqueId
			?: throw InvalidCommandArgument("Console must specify a player!")

		val name = SLPlayer.getName(playerID.slPlayerId) ?: fail { "Player not found" }

		val (xp: Int?, level: Int?) = SLPlayer.getXPAndLevel(
			playerID.slPlayerId
		) ?: throw ConditionFailedException("$name doesn't have any XP data.")

		val maxLevel: Int = MAX_LEVEL

		val isSelf: Boolean = name == sender.name

		val response: TextComponent = gray((if (isSelf) "You have " else "$name has "))

		response + when (level) {
			maxLevel -> aqua(xp.toString()) + gray(" SLXP, at max level.")
			else -> darkAqua("$xp") + aqua("/") + darkAqua(Levels.getLevelUpCost(level + 1).toString()) +
				gray(" SLXP, at level ") +
				darkPurple("$level") + lightPurple("/") + darkPurple("$maxLevel")
		}
		sender msg response
	}
}
