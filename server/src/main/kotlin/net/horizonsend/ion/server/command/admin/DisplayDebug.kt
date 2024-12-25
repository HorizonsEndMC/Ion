package net.horizonsend.ion.server.command.admin

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Subcommand
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.client.display.modular.MultiBlockDisplay
import org.bukkit.entity.Player

@CommandAlias("displaydebug")
object DisplayDebug : SLCommand() {
	@Subcommand("clipboard")
	fun clipboard(player: Player) {
		val clipboard = WorldEdit.getInstance().sessionManager.get(BukkitAdapter.adapt(player)).clipboard?.clipboards?.firstOrNull() ?: fail { "empty clipboard :(" }
		MultiBlockDisplay.createFromClipboard(player, clipboard)
	}
}
