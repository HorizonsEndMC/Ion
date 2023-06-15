package net.horizonsend.ion.server.legacy.commands

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.successActionMessage
import net.starlegacy.command.SLCommand
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object RainbowProjectileCommand : SLCommand() {
	@CommandAlias("gaymode")
	@CommandPermission("ioncore.rainbow")
	fun onExecute(sender: CommandSender, p: Player) = asyncCommand(sender) {
		val ship = getStarshipPiloting(p)

		ship.rainbowToggle = !ship.rainbowToggle
		sender.success("${if (ship.rainbowToggle) "Dea" else "A"}activated gay mode")
	}
}
