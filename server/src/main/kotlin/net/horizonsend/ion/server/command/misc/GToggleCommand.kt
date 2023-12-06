package net.horizonsend.ion.server.command.misc

import co.aikar.commands.annotation.CommandAlias
import net.horizonsend.ion.common.extensions.successActionMessage
import net.horizonsend.ion.common.extensions.userErrorActionMessage
import net.horizonsend.ion.common.utils.luckPerms
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.miscellaneous.utils.PlayerWrapper.Companion.common
import net.luckperms.api.model.group.Group
import net.luckperms.api.node.NodeEqualityPredicate
import net.luckperms.api.node.types.InheritanceNode
import org.bukkit.entity.Player

object GToggleCommand : SLCommand() {
	val noGlobalGroup: Group? = luckPerms.groupManager.getGroup("noglobal")
	val noGlobalInheritanceNode = noGlobalGroup?.let { InheritanceNode.builder(noGlobalGroup).build() }

	@Suppress("Unused")
	@CommandAlias("gtoggle")
	fun onExecute(sender: Player) {
		noGlobalInheritanceNode ?: fail { "noglobal group not found" }

		val user = sender.common().getUser()

		if (user.data().contains(noGlobalInheritanceNode, NodeEqualityPredicate.IGNORE_EXPIRY_TIME).asBoolean()) {
			user.data().remove(noGlobalInheritanceNode)
			sender.successActionMessage("Global chat shown")
		} else {
			user.data().add(noGlobalInheritanceNode)
			sender.userErrorActionMessage("Global chat hidden")
		}
	}

	override fun supportsVanilla(): Boolean {
		return true
	}
}
