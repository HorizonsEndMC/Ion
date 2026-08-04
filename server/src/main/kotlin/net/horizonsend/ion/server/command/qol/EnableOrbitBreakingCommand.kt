package net.horizonsend.ion.server.command.qol

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.listener.misc.ProtectionListener
import org.bukkit.entity.Player

@CommandAlias("orbitbreak")
object EnableOrbitBreakingCommand : SLCommand() {
    @Default
    fun onOrbitBreak(sender: Player) = asyncCommand(sender) {
        ProtectionListener.orbitBreakEnable.add(sender.uniqueId)
        sender.success("Enabled breaking blocks in planet orbit until server restarts")
    }
}