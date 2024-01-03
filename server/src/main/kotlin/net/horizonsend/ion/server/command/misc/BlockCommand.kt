package net.horizonsend.ion.server.command.misc

import co.aikar.commands.annotation.CommandAlias
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import org.bukkit.entity.Player
import org.litote.kmongo.addToSet
import org.litote.kmongo.pull

object BlockCommand : SLCommand() {
	@CommandAlias("block")
	@Suppress("unused")
	fun onBlock(sender: Player, player: String) = asyncCommand(sender) {
		val resolvedPlayer = SLPlayer[player] ?: fail { "Player $player not found!" }
		val cached = PlayerCache[sender]

		if (cached.blockedPlayerIDs.contains(resolvedPlayer._id)) fail { "$player is already blocked!" }

		SLPlayer.updateById(sender.slPlayerId, addToSet(SLPlayer::blockedPlayerIDs, resolvedPlayer._id))

		sender.success("Blocked $player")
	}

	@CommandAlias("unblock")
	@Suppress("unused")
	fun onUnblock(sender: Player, player: String) = asyncCommand(sender) {
		val resolvedPlayer = SLPlayer[player] ?: fail { "Player $player not found!" }
		val cached = PlayerCache[sender]

		if (!cached.blockedPlayerIDs.contains(resolvedPlayer._id)) fail { "$player is not blocked!" }

		SLPlayer.updateById(sender.slPlayerId, pull(SLPlayer::blockedPlayerIDs, resolvedPlayer._id))

		sender.success("Unblocked $player")
	}
}
