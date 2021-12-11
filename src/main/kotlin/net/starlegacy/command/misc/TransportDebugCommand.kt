package net.starlegacy.command.misc

import net.starlegacy.command.SLCommand
import net.starlegacy.feature.transport.Extractors
import net.starlegacy.feature.transport.TransportConfig
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import net.starlegacy.util.msg
import org.bukkit.command.CommandSender

@CommandPermission("starlegacy.transportdebug")
@CommandAlias("transportdebug|transportbug")
object TransportDebugCommand : SLCommand() {
    @Subcommand("reload")
    fun reload(sender: CommandSender) {
        TransportConfig.reload()
        sender msg "&aReloaded config"
    }

    @Subcommand("clearbusy")
    fun onClearBusy(sender: CommandSender) {
        Extractors.BUSY_PIPE_EXTRACTORS.clear()
    }
}
