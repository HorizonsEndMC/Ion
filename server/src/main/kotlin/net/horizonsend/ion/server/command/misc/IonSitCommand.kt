package net.horizonsend.ion.server.command.misc

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Default
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.utils.luckPerms
import net.horizonsend.ion.server.command.SLCommand
import net.luckperms.api.node.types.PermissionNode
import org.bukkit.entity.Player

@CommandAlias("sittoggle")
object IonSitCommand : SLCommand() {
	@CommandCompletion("true|false")
	@Default
	fun enableSitting(sender: Player, allow: Boolean) {
		luckPerms.userManager.modifyUser(sender.uniqueId) { user ->
			user.data().add(sitStateNode.toBuilder().value(allow).build())
			sender.success("Changed sitting to $allow")
		}
	}

	val sitStateNode = PermissionNode.builder("ion.sit.allowed").build()
}
