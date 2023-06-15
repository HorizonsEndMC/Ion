package net.horizonsend.ion.server.legacy.commands

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import net.horizonsend.ion.common.extensions.successActionMessage
import net.starlegacy.command.SLCommand
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object RainbowProjectileCommand : SLCommand() {
	@CommandAlias("rainbowweapon")
	@CommandPermission("ioncore.rainbow")
	fun onExecute(sender: CommandSender, p: Player) = asyncCommand(sender) {
		val ship = getStarshipPiloting(p)
		if (!ship.rainbowToggle) {
			ship.rainbowToggle = true
			sender.successActionMessage("Rainbow Weapon Colors activated!")
		} else {
			ship.rainbowToggle = !ship.rainbowToggle
			sender.successActionMessage("Rainbow Weapon Colours De-Activating")
		}
	}
}
