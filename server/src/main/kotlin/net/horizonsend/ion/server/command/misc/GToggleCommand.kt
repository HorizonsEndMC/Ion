package net.horizonsend.ion.server.command.misc

import co.aikar.commands.annotation.CommandAlias
import net.horizonsend.ion.common.extensions.successActionMessage
import net.horizonsend.ion.common.utils.luckPerms
import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.model.group.Group
import net.luckperms.api.model.user.User
import net.luckperms.api.node.NodeEqualityPredicate
import net.horizonsend.ion.server.command.SLCommand
import org.bukkit.entity.Player

object GToggleCommand : SLCommand() {
	@Suppress("Unused")
	@CommandAlias("gtoggle")
	fun onExecute(sender: Player) {
		val group: Group = luckPerms.groupManager.getGroup("noglobal") ?: fail { "noglobal group not found" }
		val user: User = luckPerms.userManager.getUser(sender.uniqueId) ?: fail { "Failed to get user data" }

		val groupNode = luckPerms.nodeBuilderRegistry.forInheritance().group(group).value(true).build()

		if (user.data().contains(groupNode, NodeEqualityPredicate.IGNORE_EXPIRY_TIME).asBoolean()) {
			user.data().remove(groupNode)
			sender.successActionMessage("Global chat shown")
		} else {
			user.data().add(groupNode)
			sender.successActionMessage("&cGlobal chat hidden")
		}
	}

	override fun supportsVanilla(): Boolean {
		return true
	}
}
