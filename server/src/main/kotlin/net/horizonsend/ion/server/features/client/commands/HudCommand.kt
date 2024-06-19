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
    @Subcommand("planets image")
    @Suppress("unused")
    fun onTogglePlanetsImage(
        sender: Player,
        @Optional toggle: Boolean?
    ) {
        val hudPlanetsImage = toggle ?: !PlayerCache[sender].hudPlanetsImage
        SLPlayer.updateById(sender.slPlayerId, setValue(SLPlayer::hudPlanetsImage, hudPlanetsImage))
        PlayerCache[sender].hudPlanetsImage = hudPlanetsImage
        sender.success("Changed planet visibility in HUD to $hudPlanetsImage")
    }

    @Subcommand("planets selector")
    @Suppress("unused")
    fun onTogglePlanetsSelector(
        sender: Player,
        @Optional toggle: Boolean?
    ) {
        val hudPlanetsSelector = toggle ?: !PlayerCache[sender].hudPlanetsSelector
        SLPlayer.updateById(sender.slPlayerId, setValue(SLPlayer::hudPlanetsSelector, hudPlanetsSelector))
        PlayerCache[sender].hudPlanetsSelector = hudPlanetsSelector
        sender.success("Changed planet selector visibility in HUD to $hudPlanetsSelector")
    }
}