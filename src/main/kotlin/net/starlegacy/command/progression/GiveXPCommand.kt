package net.starlegacy.command.progression

import net.starlegacy.command.SLCommand
import net.starlegacy.feature.progression.SLXP
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import net.starlegacy.util.msg
import org.bukkit.command.CommandSender

object GiveXPCommand : SLCommand() {
    @CommandAlias("givexp")
    @CommandPermission("advance.givexp")
    fun execute(sender: CommandSender, target: OnlinePlayer, amount: Int) {
        val player = target.player

        SLXP.addAsync(player, amount)
        sender msg "&2Gave &d$amount&2 XP to &d${player.name}"
    }
}
