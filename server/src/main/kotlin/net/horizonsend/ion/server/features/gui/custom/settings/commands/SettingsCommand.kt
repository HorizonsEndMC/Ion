package net.horizonsend.ion.server.features.gui.custom.settings.commands

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.gui.custom.settings.SettingsMainMenuGui
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import org.bukkit.entity.Player
import org.litote.kmongo.setValue

@CommandAlias("settings")
object SettingsCommand : SLCommand() {

    @Default
    @Suppress("unused")
    fun onSettings(sender: Player) {
        SettingsMainMenuGui(sender).openMainWindow()
    }


    @Suppress("unused")
    @Subcommand("sound")
    fun onToggleEnableAdditionalSounds(
        sender: Player,
        @Optional toggle: Boolean?
    ) {
        val enableAdditionalSounds = toggle ?: !PlayerCache[sender].enableAdditionalSounds
        SLPlayer.updateById(sender.slPlayerId, setValue(SLPlayer::enableAdditionalSounds, enableAdditionalSounds))
        PlayerCache[sender].enableAdditionalSounds = enableAdditionalSounds
        sender.success("Changed enabled additional sounds to $enableAdditionalSounds")
    }
}