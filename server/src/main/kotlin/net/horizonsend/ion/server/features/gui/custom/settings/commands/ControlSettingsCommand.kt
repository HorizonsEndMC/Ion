package net.horizonsend.ion.server.features.gui.custom.settings.commands

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Optional
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import org.bukkit.entity.Player
import org.litote.kmongo.setValue

@CommandAlias("control")
object ControlSettingsCommand : SLCommand() {
    @CommandAlias("enableAlternateDCCruise")
    @CommandCompletion("true|false")
    @Suppress("unused")
    fun onUseAlternateDCCruise(sender: Player, @Optional toggle: Boolean?) {
        val useAlternateDcCruise = toggle ?: !PlayerCache[sender].useAlternateDCCruise
        SLPlayer.updateById(sender.slPlayerId, setValue(SLPlayer::useAlternateDCCruise, useAlternateDcCruise))
        PlayerCache[sender.uniqueId].useAlternateDCCruise = useAlternateDcCruise
        sender.success("Changed alternate DC cruise to $useAlternateDcCruise")
        if (useAlternateDcCruise) {
            sender.success("Activating cruise while in direct control will override DC")
        } else {
            sender.success("Direct control will not be overriden by cruise")
        }
    }

    @CommandAlias("dcSpeedModifier")
    @Suppress("unused")
    fun onChangeDcModifier(sender: Player) {
        val currentSetting = PlayerCache[sender.uniqueId].dcSpeedModifier

        val newSetting = if (currentSetting < 5) currentSetting + 1 else 1
        SLPlayer.updateById(sender.slPlayerId, setValue(SLPlayer::dcSpeedModifier, newSetting))
        PlayerCache[sender].dcSpeedModifier = newSetting
        sender.success("Changed DC speed modifier to $newSetting")
    }
}