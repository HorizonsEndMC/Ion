package net.horizonsend.ion.server.features.gui.custom.settings.commands

import co.aikar.commands.annotation.CommandAlias
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

@CommandAlias("sound")
object SoundSettingsCommand : SLCommand() {

    @Default
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

    @Suppress("unused")
    @Subcommand("sound")
    fun onChangeCruiseIndicatorSound(sender: Player) {
        val currentSetting = PlayerCache[sender.uniqueId].soundCruiseIndicator

        // Keep newSetting in the range of the number of sort options
        val newSetting = if (currentSetting < 2) currentSetting + 1 else 0
        SLPlayer.updateById(sender.slPlayerId, setValue(SLPlayer::soundCruiseIndicator, newSetting))
        PlayerCache[sender].soundCruiseIndicator = newSetting
        sender.success("Changed cruise indicator sound to ${CruiseIndicatorSounds.entries[newSetting]}")
    }

    enum class CruiseIndicatorSounds {
        OFF,
        SHORT,
        LONG
    }
}