package net.horizonsend.ion.server.features.client.commands

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import org.bukkit.entity.Player
import org.litote.kmongo.setValue

@CommandAlias("itemsearchToggle")
@CommandPermission("ion.searchtoggle")
object ItemSearchToggleCommand : SLCommand() {
	@Default
	@CommandCompletion("true|false")
	fun default(player: Player, @Optional toggle: Boolean?) {
		val showItemDisplay = toggle ?: !PlayerCache[player].showItemSearchItem
		SLPlayer.updateById(player.slPlayerId, setValue(SLPlayer::showItemSearchItem, showItemDisplay))
		player.success("Changed showing searched item to $showItemDisplay")
	}
}
