package net.horizonsend.ion.server.command.misc

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.utils.luckPerms
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.gui.custom.settings.SettingsOtherGui.Companion.sitStateNode
import org.bukkit.entity.Player

@CommandAlias("sittoggle")
object IonSitCommand : SLCommand() {
	@CommandCompletion("true|false")
	fun enableSitting(sender: Player, allow: Boolean) {
		luckPerms.userManager.modifyUser(sender.uniqueId) { user ->
			user.data().add(sitStateNode.toBuilder().value(allow).build())
			sender.success("Changed sitting to $allow")
		}
	}
}
