package net.horizonsend.ion.server.command.starship

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Subcommand
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import net.horizonsend.ion.common.extensions.successActionMessage
import net.horizonsend.ion.server.command.SLCommand
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@Suppress("Unused")
@CommandAlias("targetrandom")
@CommandPermission("ion.core.random")
object RandomTargetCommand : SLCommand() {
	@Subcommand("toggle")
	@Description("Toggle random targeting")
	fun onToggleRandom(sender: CommandSender, p: Player) = asyncCommand(sender) {
		val ship = getStarshipPiloting(p)
		if (!ship.randomTarget) {
			ship.randomTarget = true
			sender.successActionMessage("Random Turret Targeting activated!")
		} else {
			ship.randomTarget = false
			sender.successActionMessage("Random Turret Targeting De-Activating")
		}
	}

	@Subcommand("blacklist")
	@CommandCompletion("@nothing add|remove @nothing")
	@Description("Players you wish to blacklist from being targeted")
	fun onTargetBlacklist(sender: CommandSender, p: Player, add: String, player: OnlinePlayer) = asyncCommand(sender) {
		val ship = getStarshipPiloting(p)
		if (add.lowercase().contains("add")) {
			ship.randomTargetBlacklist.add(player.getPlayer().uniqueId)
		} else {
			ship.randomTargetBlacklist.remove(player.getPlayer().uniqueId)
		}
		sender.successActionMessage("Added $p to Random Target Blacklist")
	}

	@Subcommand("clearblacklist")
	@Description("clears the blacklist")
	fun onClearBlacklist(sender: CommandSender, p: Player) = asyncCommand(sender) {
		val ship = getStarshipPiloting(p)
		ship.randomTargetBlacklist.clear()
		sender.successActionMessage("Cleared Random Target Blacklist")
	}
}
