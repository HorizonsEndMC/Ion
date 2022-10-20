package net.horizonsend.ion.core.commands

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import net.horizonsend.ion.core.feedback.FeedbackType.SUCCESS
import net.horizonsend.ion.core.feedback.sendFeedbackActionMessage
import net.starlegacy.command.SLCommand
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object RainbowProjectileCommand : SLCommand() {
	@CommandAlias("rainbowweapon")
	@CommandPermission("ioncore.rainbow")
	fun onExecute(sender: CommandSender, p: Player) = asyncCommand(sender) {
		val ship = getStarshipPiloting(p)
		if (!ship.rainbowtoggle) {
			ship.rainbowtoggle = true
			sender.sendFeedbackActionMessage(SUCCESS, "Rainbow Weapon Colors activated!")
		} else {
			ship.rainbowtoggle = !ship.rainbowtoggle
			sender.sendFeedbackActionMessage(SUCCESS, "Rainbow Weapon Colours De-Activating")
		}
	}
}