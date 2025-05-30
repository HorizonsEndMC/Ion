package net.horizonsend.ion.server.features.client.commands

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.database.schema.misc.PlayerSettings
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.cache.PlayerSettingsCache.getSetting
import net.horizonsend.ion.server.features.cache.PlayerSettingsCache.setSetting
import org.bukkit.entity.Player

@CommandAlias("hud")
object HudCommand : SLCommand() {
    @Subcommand("planets")
    fun onToggleHudPlanets(
        sender: Player,
        @Optional toggle: Boolean?
    ) {
        val hudPlanetsImage = toggle ?: !sender.getSetting(PlayerSettings::hudPlanetsImage)
        sender.setSetting(PlayerSettings::hudPlanetsImage, hudPlanetsImage)

        sender.success("Changed planet visibility in HUD to $hudPlanetsImage")
    }

    @Subcommand("selector")
    fun onToggleHudSelector(
        sender: Player,
        @Optional toggle: Boolean?
    ) {
        val hudPlanetsSelector = toggle ?: !sender.getSetting(PlayerSettings::hudPlanetsSelector)
        sender.setSetting(PlayerSettings::hudPlanetsSelector, hudPlanetsSelector)

        sender.success("Changed planet selector visibility in HUD to $hudPlanetsSelector")
    }

    @Subcommand("stars")
    fun onToggleHudStars(
        sender: Player,
        @Optional toggle: Boolean?
    ) {
        val hudIconStars = toggle ?: !sender.getSetting(PlayerSettings::hudIconStars)
        sender.setSetting(PlayerSettings::hudIconStars, hudIconStars)

        sender.success("Changed star visibility in HUD to $hudIconStars")
    }

    @Subcommand("beacons")
    fun onToggleHudBeacons(
        sender: Player,
        @Optional toggle: Boolean?
    ) {
        val hudIconBeacons = toggle ?: !sender.getSetting(PlayerSettings::hudIconBeacons)
        sender.setSetting(PlayerSettings::hudIconBeacons, hudIconBeacons)

        sender.success("Changed beacon visibility in HUD to $hudIconBeacons")
    }

    @Subcommand("stations")
    fun onToggleHudStations(
        sender: Player,
        @Optional toggle: Boolean?
    ) {
        val hudIconStations = toggle ?: !sender.getSetting(PlayerSettings::hudIconStations)
        sender.setSetting(PlayerSettings::hudIconStations, hudIconStations)

        sender.success("Changed station visibility in HUD to $hudIconStations")
    }

    @Subcommand("bookmarks")
    fun onToggleHudBookmarks(
        sender: Player,
        @Optional toggle: Boolean?
    ) {
        val hudIconBookmarks = toggle ?: !sender.getSetting(PlayerSettings::hudIconBookmarks)
        sender.setSetting(PlayerSettings::hudIconBookmarks, hudIconBookmarks)

        sender.success("Changed bookmark visibility in HUD to $hudIconBookmarks")
    }
}
