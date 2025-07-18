package net.horizonsend.ion.server.features.gui.custom.settings.commands

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.database.schema.misc.PlayerSettings
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.cache.PlayerSettingsCache.getSetting
import net.horizonsend.ion.server.features.cache.PlayerSettingsCache.setSetting
import org.bukkit.entity.Player

@CommandAlias("sound")
object SoundSettingsCommand : SLCommand() {

    @Default
    @Subcommand("sound")
    fun onToggleEnableAdditionalSounds(
        sender: Player,
        @Optional toggle: Boolean?
    ) {
        val enableAdditionalSounds = toggle ?: !sender.getSetting(PlayerSettings::enableAdditionalSounds)
		sender.setSetting(PlayerSettings::enableAdditionalSounds, enableAdditionalSounds)

		sender.success("Changed enabled additional sounds to $enableAdditionalSounds")
    }

    @Subcommand("cruiseIndicatorSound")
    fun onChangeCruiseIndicatorSound(sender: Player) {
        val currentSetting = sender.getSetting(PlayerSettings::soundCruiseIndicator)

        // Keep newSetting in the range of the number of sort options
        val newSetting = if (currentSetting < 2) currentSetting + 1 else 0
		sender.setSetting(PlayerSettings::soundCruiseIndicator, newSetting)

		sender.success("Changed cruise indicator sound to ${CruiseIndicatorSounds.entries[newSetting]}")
    }

	@Subcommand("hitmarkerOnHullSound")
	fun onChangeHitmarkerOnHullSound(sender: Player, @Optional toggle: Boolean?) {
		val hitmarkerOnHull = toggle ?: !sender.getSetting(PlayerSettings::hitmarkerOnHull)
		sender.setSetting(PlayerSettings::hitmarkerOnHull, hitmarkerOnHull)

		sender.success("Changed hitmarker one hull sound to $hitmarkerOnHull")
	}

    enum class CruiseIndicatorSounds {
        OFF,
        SHORT,
        LONG
    }
}
