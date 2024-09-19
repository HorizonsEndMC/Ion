package net.horizonsend.ion.server.command.misc

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.transport.old.Extractors
import net.horizonsend.ion.server.features.transport.old.TransportConfig
import net.horizonsend.ion.server.features.transport.old.Wires
import org.bukkit.command.CommandSender
import java.util.concurrent.Executors

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

	@Suppress("Unused")
	@Subcommand("clear wirequeue")
	fun onClearQueue(sender: CommandSender) {
		val wireQueue = Wires.thread

		wireQueue.shutdownNow()
		Wires.thread = Executors.newSingleThreadExecutor(Wires.threadFactory)
	}
}
