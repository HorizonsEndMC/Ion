package net.horizonsend.ion.server.features.sidebar.command

import co.aikar.commands.annotation.CommandAlias
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
object SidebarCombatTimerCommand : SLCommand() {
    @Subcommand("combattimer")
    fun defaultCase(
        sender: Player
    ) {
        sender.userError("Usage: /sidebar combattimer <option> [toggle]")
    }

    @Subcommand("combattimer enable")
    fun onEnableCombatTimer(
        sender: Player
    ) {
        SLPlayer.updateById(sender.slPlayerId, setValue(SLPlayer::combatTimerEnabled, true))
        PlayerCache[sender].combatTimerEnabled = true
        sender.success("Enabled combat timer info on sidebar")
    }

    @Subcommand("combattimer disable")
    fun onDisableCombatTimer(
        sender: Player
    ) {
        SLPlayer.updateById(sender.slPlayerId, setValue(SLPlayer::combatTimerEnabled, false))
        PlayerCache[sender].combatTimerEnabled = false
        sender.success("Disabled combat timer info on sidebar")
    }
}
