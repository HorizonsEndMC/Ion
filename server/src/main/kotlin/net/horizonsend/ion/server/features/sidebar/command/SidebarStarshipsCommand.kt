package net.horizonsend.ion.server.features.sidebar.command

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import org.bukkit.entity.Player
import org.litote.kmongo.setValue

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
        SLPlayer.updateById(sender.slPlayerId, setValue(SLPlayer::starshipsEnabled, true))
        PlayerCache[sender].starshipsEnabled = true
        sender.success("Enabled starship info on sidebar")
    }

    @Subcommand("starship disable")
    fun onDisableStarships(
        sender: Player
    ) {
        SLPlayer.updateById(sender.slPlayerId, setValue(SLPlayer::starshipsEnabled, false))
        PlayerCache[sender].starshipsEnabled = false
        sender.success("Disabled starship info on sidebar")
    }

    @Subcommand("starship advanced")
    fun onToggleAdvancedStarshipInfo(
        sender: Player,
        @Optional toggle: Boolean?
    ) {
        val advancedStarshipInfo = toggle ?: !PlayerCache[sender].advancedStarshipInfo
        SLPlayer.updateById(sender.slPlayerId, setValue(SLPlayer::advancedStarshipInfo, advancedStarshipInfo))
        PlayerCache[sender].advancedStarshipInfo = advancedStarshipInfo
        sender.success("Changed advanced starship info to $advancedStarshipInfo")
    }

    @Subcommand("starship rotateCompass")
    fun onToggleRotateCompass(
        sender: Player,
        @Optional toggle: Boolean?
    ) {
        val rotateCompass = toggle ?: !PlayerCache[sender].rotateCompass
        SLPlayer.updateById(sender.slPlayerId, setValue(SLPlayer::rotateCompass, rotateCompass))
        PlayerCache[sender].rotateCompass = rotateCompass
        sender.success("Changed rotating compass to $rotateCompass")
    }
}
