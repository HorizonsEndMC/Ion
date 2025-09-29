package net.horizonsend.ion.server.command.qol

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.starship.subsystem.misc.MiningLaserSubsystem
import org.bukkit.entity.Player

@CommandAlias("stationbreak")
object EnableStationBreakingCommand : SLCommand() {
    @Default
    fun onStationBreak(sender: Player) {
        MiningLaserSubsystem.spaceStationBreakEnable.add(sender.uniqueId)
        sender.success("Enabled breaking blocks within a station until server restarts")
    }
}