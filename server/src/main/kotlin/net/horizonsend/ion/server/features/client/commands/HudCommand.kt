package net.horizonsend.ion.server.features.client.commands

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import org.bukkit.entity.Player
import org.litote.kmongo.setValue

@CommandAlias("hud")
object HudCommand : SLCommand() {
    @Subcommand("planets")
    fun onToggleHudPlanets(
        sender: Player,
        @Optional toggle: Boolean?
    ) {
        val hudPlanetsImage = toggle ?: !PlayerCache[sender].hudPlanetsImage
        SLPlayer.updateById(sender.slPlayerId, setValue(SLPlayer::hudPlanetsImage, hudPlanetsImage))
        PlayerCache[sender].hudPlanetsImage = hudPlanetsImage
        sender.success("Changed planet visibility in HUD to $hudPlanetsImage")
    }

    @Subcommand("selector")
    fun onToggleHudSelector(
        sender: Player,
        @Optional toggle: Boolean?
    ) {
        val hudPlanetsSelector = toggle ?: !PlayerCache[sender].hudPlanetsSelector
        SLPlayer.updateById(sender.slPlayerId, setValue(SLPlayer::hudPlanetsSelector, hudPlanetsSelector))
        PlayerCache[sender].hudPlanetsSelector = hudPlanetsSelector
        sender.success("Changed planet selector visibility in HUD to $hudPlanetsSelector")
    }

    @Subcommand("stars")
    fun onToggleHudStars(
        sender: Player,
        @Optional toggle: Boolean?
    ) {
        val hudIconStars = toggle ?: !PlayerCache[sender].hudIconStars
        SLPlayer.updateById(sender.slPlayerId, setValue(SLPlayer::hudIconStars, hudIconStars))
        PlayerCache[sender].hudIconStars = hudIconStars
        sender.success("Changed star visibility in HUD to $hudIconStars")
    }

    @Subcommand("beacons")
    fun onToggleHudBeacons(
        sender: Player,
        @Optional toggle: Boolean?
    ) {
        val hudIconBeacons = toggle ?: !PlayerCache[sender].hudIconBeacons
        SLPlayer.updateById(sender.slPlayerId, setValue(SLPlayer::hudIconBeacons, hudIconBeacons))
        PlayerCache[sender].hudIconBeacons = hudIconBeacons
        sender.success("Changed beacon visibility in HUD to $hudIconBeacons")
    }

    @Subcommand("stations")
    fun onToggleHudStations(
        sender: Player,
        @Optional toggle: Boolean?
    ) {
        val hudIconStations = toggle ?: !PlayerCache[sender].hudIconStations
        SLPlayer.updateById(sender.slPlayerId, setValue(SLPlayer::hudIconStations, hudIconStations))
        PlayerCache[sender].hudIconStations = hudIconStations
        sender.success("Changed station visibility in HUD to $hudIconStations")
    }

    @Subcommand("bookmarks")
    fun onToggleHudBookmarks(
        sender: Player,
        @Optional toggle: Boolean?
    ) {
        val hudIconBookmarks = toggle ?: !PlayerCache[sender].hudIconBookmarks
        SLPlayer.updateById(sender.slPlayerId, setValue(SLPlayer::hudIconBookmarks, hudIconBookmarks))
        PlayerCache[sender].hudIconBookmarks = hudIconBookmarks
        sender.success("Changed bookmark visibility in HUD to $hudIconBookmarks")
    }
}
