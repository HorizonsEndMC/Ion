package net.horizonsend.ion.server.command.misc

import co.aikar.commands.annotation.CommandAlias
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.successActionMessage
import net.horizonsend.ion.common.extensions.userErrorActionMessage
import net.horizonsend.ion.common.utils.luckPerms
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.miscellaneous.utils.CommonPlayerWrapper.Companion.common
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.luckperms.api.node.NodeEqualityPredicate
import net.luckperms.api.node.types.PermissionNode
import org.bukkit.entity.Player
import org.litote.kmongo.addToSet
import org.litote.kmongo.pull

object DiscordBlockCommand : SLCommand() {
	private val noDiscordNode = PermissionNode.builder("ion.discord.hidden").build()

	@CommandAlias("discordblock")
	fun onBlock(sender: Player, discordUser: String) = asyncCommand(sender) {
		val user = discordUser.trim()
		if (user.isBlank()) fail { "Enter a Discord username or user ID." }

		val blockedUsers = PlayerCache[sender].blockedDiscordUsers
		if (blockedUsers.any { it.equals(user, ignoreCase = true) }) fail { "$user is already blocked." }

		SLPlayer.updateById(sender.slPlayerId, addToSet(SLPlayer::blockedDiscordUsers, user))
		PlayerCache[sender].blockedDiscordUsers = blockedUsers + user
		sender.success("Blocked Discord user $user.")
	}

	@CommandAlias("discordunblock")
	fun onUnblock(sender: Player, discordUser: String) = asyncCommand(sender) {
		val user = discordUser.trim()
		val blockedUser = PlayerCache[sender].blockedDiscordUsers.firstOrNull { it.equals(user, ignoreCase = true) }
			?: fail { "$user is not blocked." }

		SLPlayer.updateById(sender.slPlayerId, pull(SLPlayer::blockedDiscordUsers, blockedUser))
		PlayerCache[sender].blockedDiscordUsers = PlayerCache[sender].blockedDiscordUsers - blockedUser
		sender.success("Unblocked Discord user $blockedUser.")
	}

	@CommandAlias("discordtoggle")
	fun onToggle(sender: Player) {
		luckPerms.userManager.modifyUser(sender.uniqueId) { user ->
			if (user.data().contains(noDiscordNode, NodeEqualityPredicate.EXACT).asBoolean()) {
				user.data().remove(noDiscordNode)
				sender.successActionMessage("Discord chat shown")
			} else {
				user.data().add(noDiscordNode)
				sender.userErrorActionMessage("Discord chat hidden")
			}
		}
	}

	fun isDiscordHidden(player: Player): Boolean = player.common().getUser()
		.data()
		.contains(noDiscordNode, NodeEqualityPredicate.EXACT)
		.asBoolean()
}