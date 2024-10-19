package net.horizonsend.ion.server.command.admin

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.player.CombatTimer
import org.bukkit.entity.Player

@CommandAlias("combattag")
object CombatTagCommand : SLCommand() {

    @Subcommand("npc give")
    @Suppress("unused")
    @CommandPermission("ion.combattag")
    @CommandCompletion("@players")
    @Description("Apply an NPC combat tag to a player")
    fun onGiveNpcCombatTag(sender: Player, target: String) {
        val player = resolveOfflinePlayer(target)

        CombatTimer.addNpcCombatTag(player)
        sender.success("Gave $target an NPC combat tag")
    }

    @Subcommand("npc remove")
    @Suppress("unused")
    @CommandPermission("ion.combattag")
    @CommandCompletion("@players")
    @Description("Remove an NPC combat tag from a player")
    fun onRemoveNpcCombatTag(sender: Player, target: String) {
        val player = resolveOfflinePlayer(target)

        CombatTimer.removeNpcCombatTag(player)
        sender.success("Removed NPC combat tag from $target")
    }

    @Subcommand("pvp give")
    @Suppress("unused")
    @CommandPermission("ion.combattag")
    @CommandCompletion("@players")
    @Description("Apply a PvP combat tag to a player")
    fun onGivePvpCombatTag(sender: Player, target: String) {
        val player = resolveOfflinePlayer(target)

        CombatTimer.addPvpCombatTag(player)
        sender.success("Gave $target a PvP combat tag")
    }

    @Subcommand("pvp remove")
    @Suppress("unused")
    @CommandPermission("ion.combattag")
    @CommandCompletion("@players")
    @Description("Remove a PvP combat tag from a player")
    fun onRemovePvpCombatTag(sender: Player, target: String) {
        val player = resolveOfflinePlayer(target)

        CombatTimer.removePvpCombatTag(player)
        sender.success("Removed PvP combat tag from $target")
    }
}