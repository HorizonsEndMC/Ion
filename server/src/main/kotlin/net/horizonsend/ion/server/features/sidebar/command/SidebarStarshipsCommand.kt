package net.horizonsend.ion.server.features.sidebar.command

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import org.bukkit.entity.Player
import org.litote.kmongo.set
import org.litote.kmongo.setTo

@CommandAlias("sidebar")
object SidebarStarshipsCommand : SLCommand() {
    @Default
    @Subcommand("starship")
    @Suppress("unused")
    fun defaultCase(
        sender: Player
    ) {
        sender.userError("Usage: /sidebar starship <option> [toggle]")
    }

    @Suppress("unused")
    @Subcommand("starship enable")
    fun onEnableStarships(
        sender: Player
    ) {
        SLPlayer.updateById(sender.slPlayerId, set(SLPlayer::starshipsEnabled setTo true))
        sender.success("Enabled starship info on sidebar")
    }

    @Suppress("unused")
    @Subcommand("starship disable")
    fun onDisableStarships(
        sender: Player
    ) {
        SLPlayer.updateById(sender.slPlayerId, set(SLPlayer::starshipsEnabled setTo false))
        sender.success("Disabled starship info on sidebar")
    }

    @Suppress("unused")
    @Subcommand("starship advanced")
    fun onToggleAdvancedStarshipInfo(
        sender: Player,
        @Optional toggle: Boolean?
    ) {
        val advancedStarshipInfo = toggle ?: !PlayerCache[sender].advancedStarshipInfo
        SLPlayer.updateById(sender.slPlayerId, set(SLPlayer::advancedStarshipInfo setTo advancedStarshipInfo))
        sender.success("Changed advanced starship info to $advancedStarshipInfo")
    }

    @Suppress("unused")
    @Subcommand("starship rotateCompass")
    fun onToggleRotateCompass(
        sender: Player,
        @Optional toggle: Boolean?
    ) {
        val rotateCompass = toggle ?: !PlayerCache[sender].rotateCompass
        SLPlayer.updateById(sender.slPlayerId, set(SLPlayer::rotateCompass setTo rotateCompass))
        sender.success("Changed rotating compass to $rotateCompass")
    }
}