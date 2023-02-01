package net.starlegacy.command.misc

import co.aikar.commands.annotation.CommandAlias
import net.horizonsend.ion.server.legacy.feedback.FeedbackType
import net.horizonsend.ion.server.legacy.feedback.sendFeedbackActionMessage
import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.model.group.Group
import net.luckperms.api.model.user.User
import net.luckperms.api.node.NodeEqualityPredicate
import net.starlegacy.command.SLCommand
import org.bukkit.entity.Player

object GToggleCommand : SLCommand() {
	@Suppress("Unused")
	@CommandAlias("gtoggle")
	fun onExecute(sender: Player) {
		val api = LuckPermsProvider.get()
		val group: Group = api.groupManager.getGroup("noglobal") ?: fail { "noglobal group not found" }
		val user: User = api.userManager.getUser(sender.uniqueId) ?: fail { "Failed to get user data" }

		val groupNode = api.nodeBuilderRegistry.forInheritance().group(group).value(true).build()

		if (user.data().contains(groupNode, NodeEqualityPredicate.IGNORE_EXPIRY_TIME).asBoolean()) {
			user.data().remove(groupNode)
			sender.sendFeedbackActionMessage(FeedbackType.SUCCESS, "Global chat shown")
		} else {
			user.data().add(groupNode)
			sender.sendFeedbackActionMessage(FeedbackType.SUCCESS, "&cGlobal chat hidden")
		}
	}

	override fun supportsVanilla(): Boolean {
		return true
	}
}
