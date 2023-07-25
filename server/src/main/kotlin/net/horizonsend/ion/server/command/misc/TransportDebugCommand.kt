package net.horizonsend.ion.server.command.misc

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.transport.Extractors
import net.horizonsend.ion.server.features.transport.TransportConfig
import org.bukkit.command.CommandSender

@CommandPermission("starlegacy.transportdebug")
@CommandAlias("transportdebug|transportbug")
object TransportDebugCommand : SLCommand() {
	@Suppress("Unused")
	@Subcommand("reload")
	fun reload(sender: CommandSender) {
		TransportConfig.reload()
		sender.success("Reloaded config")
	}

	@Suppress("Unused")
	@Subcommand("clearbusy")
	fun onClearBusy(sender: CommandSender) {
		Extractors.BUSY_PIPE_EXTRACTORS.clear()
	}
}
