package net.horizonsend.ion.server.command.progression

import co.aikar.commands.ConditionFailedException
import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Optional
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.slPlayerId
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.progression.Levels
import net.horizonsend.ion.server.features.progression.MAX_LEVEL
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.UUID

object XPCommand : SLCommand() {
	@Suppress("Unused")
	@CommandAlias("hexp|xp")
	fun execute(sender: CommandSender, @Optional player: String?) = asyncCommand(sender) {
		val playerID: UUID = player?.let { resolveOfflinePlayer(it) }
			?: (sender as? Player)?.uniqueId
			?: throw InvalidCommandArgument("Console must specify a player!")

		val name = SLPlayer.getName(playerID.slPlayerId) ?: fail { "Player not found" }

		val (xp: Int, level: Int) = SLPlayer.getXPAndLevel(
			playerID.slPlayerId
		) ?: throw ConditionFailedException("$name doesn't have any XP data.")

		val maxLevel: Int = MAX_LEVEL

		val isSelf: Boolean = name == sender.name

		val response = (if (isSelf) "<gray>You have " else "<gray>$name has ")

		val responseLevel = when (level) {
			maxLevel -> "<aqua>$xp <gray>XP, at max level."
			else -> "<dark_aqua>$xp<aqua>/<dark_aqua>${Levels.getLevelUpCost(level + 1)}" +
				"<gray> XP, at level <dark_purple>$level<light_purple>/<dark_purple>$maxLevel"
		}
		sender.sendRichMessage("$response$responseLevel")
	}
}
