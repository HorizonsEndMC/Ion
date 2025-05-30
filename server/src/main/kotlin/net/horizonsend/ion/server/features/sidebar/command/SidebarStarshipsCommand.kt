package net.horizonsend.ion.server.features.sidebar.command

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.database.schema.misc.PlayerSettings
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.cache.PlayerSettingsCache.getSetting
import net.horizonsend.ion.server.features.cache.PlayerSettingsCache.setSetting
import org.bukkit.entity.Player

@CommandAlias("sidebar")
object SidebarStarshipsCommand : SLCommand() {
    @Subcommand("starship")
    fun defaultCase(
        sender: Player
    ) {
        sender.userError("Usage: /sidebar starship <option> [toggle]")
    }

    @Subcommand("starship enable")
    fun onEnableStarships(
        sender: Player
    ) {
		sender.setSetting(PlayerSettings::starshipsEnabled, true)

		sender.success("Enabled starship info on sidebar")
    }

    @Subcommand("starship disable")
    fun onDisableStarships(
        sender: Player
    ) {
		sender.setSetting(PlayerSettings::starshipsEnabled, true)

		sender.success("Disabled starship info on sidebar")
    }

    @Subcommand("starship advanced")
    fun onToggleAdvancedStarshipInfo(
        sender: Player,
        @Optional toggle: Boolean?
    ) {
        val advancedStarshipInfo = toggle ?: !sender.getSetting(PlayerSettings::advancedStarshipInfo)
		sender.setSetting(PlayerSettings::advancedStarshipInfo, advancedStarshipInfo)

		sender.success("Changed advanced starship info to $advancedStarshipInfo")
    }

    @Subcommand("starship rotateCompass")
    fun onToggleRotateCompass(
        sender: Player,
        @Optional toggle: Boolean?
    ) {
        val rotateCompass = toggle ?: !sender.getSetting(PlayerSettings::rotateCompass)
		sender.setSetting(PlayerSettings::rotateCompass, rotateCompass)

		sender.success("Changed rotating compass to $rotateCompass")
    }
}
