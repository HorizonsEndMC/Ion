package net.horizonsend.ion.server.command.qol

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import net.horizonsend.ion.server.miscellaneous.utils.pasteClipboardCustomBlocks
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.server.command.SLCommand
import net.kyori.adventure.text.Component.text
import org.bukkit.entity.Player

@CommandAlias("paste|ionpaste|ionPaste")
@CommandPermission("worldedit.clipboard.paste")
object CustomBlockCopyPasteFixCommand : SLCommand() {
	@Default
	@CommandCompletion("-a")
	fun command(
		player: Player,
		@Optional flag: String?
	) = asyncCommand(player) {
		val ignoreAir = when (flag?.lowercase()) {
			null -> false
			"-a" -> true
			else -> fail { "Unknown option: $flag" }
		}
		player.pasteClipboardCustomBlocks(ignoreAir)
		player.sendMessage(
			text(
				if (ignoreAir) "Pasted without air" else "Pasted",
				HE_MEDIUM_GRAY
			)
		)
	}
}
