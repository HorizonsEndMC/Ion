package net.starlegacy.command.misc

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import net.starlegacy.command.SLCommand
import net.starlegacy.feature.misc.PlanetSpawns
import org.bukkit.command.CommandSender

object PlanetSpawnMenuCommand : SLCommand() {
	@CommandAlias("planetspawnmenu")
	@CommandPermission("starlegacy.misc.planetspawnmenu")
	fun onExecute(sender: CommandSender, target: OnlinePlayer) {
		PlanetSpawns.openMenu(target.player)
	}
}
