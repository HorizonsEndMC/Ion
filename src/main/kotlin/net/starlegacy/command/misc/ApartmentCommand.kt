package net.starlegacy.command.misc

import net.starlegacy.command.SLCommand
import net.starlegacy.feature.space_apartments.SpaceApartments
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import net.starlegacy.util.msg
import org.bukkit.command.CommandSender

@CommandAlias("apartment")
object ApartmentCommand : SLCommand() {
    @Subcommand("send")
    @CommandPermission("apartment.send")
    fun onSend(sender: CommandSender, onlinePlayer: OnlinePlayer) {
        val player = onlinePlayer.player
        SpaceApartments.send(player)
        sender msg "&aSent ${player.name} to their apartment"
    }

    @Subcommand("paste")
    @CommandPermission("apartment.paste")
    fun onPaste(sender: CommandSender, onlinePlayer: OnlinePlayer) {
        val player = onlinePlayer.player
        SpaceApartments.paste(player)
    }
}
