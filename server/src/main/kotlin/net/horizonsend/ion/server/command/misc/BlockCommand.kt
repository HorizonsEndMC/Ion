package net.horizonsend.ion.server.command.misc

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Optional
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.utils.text.bracketed
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_BLUE
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_ORANGE
import net.horizonsend.ion.common.utils.text.formatPaginatedMenu
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.event.ClickEvent
import org.bukkit.entity.Player
import org.litote.kmongo.addToSet
import org.litote.kmongo.pull

object BlockCommand : SLCommand() {
	@CommandAlias("block")
	@Suppress("unused")
	@CommandCompletion("@players")
	fun onBlock(sender: Player, player: String) = asyncCommand(sender) {
		if (sender.name == player) fail { "You can't block yourself!" }
		val resolvedPlayer = SLPlayer[player] ?: fail { "Player $player not found!" }
		val cached = PlayerCache[sender]

		if (cached.blockedPlayerIDs.contains(resolvedPlayer._id)) fail { "$player is already blocked!" }

		SLPlayer.updateById(sender.slPlayerId, addToSet(SLPlayer::blockedPlayerIDs, resolvedPlayer._id))

		sender.success("Blocked $player")
	}

	@CommandAlias("unblock")
	@Suppress("unused")
	@CommandCompletion("@players")
	fun onUnblock(sender: Player, player: String) = asyncCommand(sender) {
		if (sender.name == player) fail { "You can't block yourself!" }
		val resolvedPlayer = SLPlayer[player] ?: fail { "Player $player not found!" }
		val cached = PlayerCache[sender]

		if (!cached.blockedPlayerIDs.contains(resolvedPlayer._id)) fail { "$player is not blocked!" }

		SLPlayer.updateById(sender.slPlayerId, pull(SLPlayer::blockedPlayerIDs, resolvedPlayer._id))

		sender.success("Unblocked $player")
	}

	@CommandAlias("blocks")
	@Suppress("unused")
	@CommandCompletion("@players")
	fun onBlocks(sender: Player, @Optional page: Int?) = asyncCommand(sender) {
		val blocked = PlayerCache[sender].blockedPlayerIDs.toList()

		val builder = text()

		builder.append(text("Currently blocked players:", HE_LIGHT_BLUE), newline())

		val body = formatPaginatedMenu(
			blocked.size,
			"/blocks",
			page ?: 1,
			maxPerPage = 10
		) { index ->
			val entry = blocked[index]
			val name = SLPlayer.getName(entry)

			val unBlock = bracketed(text("unblock", HE_LIGHT_ORANGE))
				.clickEvent(ClickEvent.runCommand("/unblock $name"))
				.hoverEvent(text("/unblock $name"))

			template(text("{0} {1}"), name, unBlock)
		}

		builder.append(body, newline())

		builder.append(text("Use /block [name] to block a player.", HE_LIGHT_BLUE))

		sender.sendMessage(builder.build())
	}
}
