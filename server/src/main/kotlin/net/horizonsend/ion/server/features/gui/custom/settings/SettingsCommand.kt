package net.horizonsend.ion.server.features.gui.custom.settings

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import net.horizonsend.ion.server.command.SLCommand
import org.bukkit.entity.Player

@CommandAlias("settings")
object SettingsCommand : SLCommand() {

    @Default
    @Suppress("unused")
    fun onSettings(sender: Player) {
        SettingsMainMenuGui.open(sender)
    }
}