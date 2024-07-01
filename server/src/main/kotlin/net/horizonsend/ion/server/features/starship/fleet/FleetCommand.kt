package net.horizonsend.ion.server.features.starship.fleet

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.command.SLCommand
import org.bukkit.Bukkit
import org.bukkit.entity.Player

@CommandAlias("fleet")
object FleetCommand : SLCommand() {
    @Subcommand("create")
    @Suppress("unused")
    fun onFleetCreate(sender: Player) {
        if (Fleets.findByMember(sender) != null) {
            sender.userError("You are already in a fleet")
            return
        }

        Fleets.create(sender)
        sender.success("Created fleet")
    }

    @Subcommand("disband")
    @Suppress("unused")
    fun onFleetDisband(sender: Player) {
        val fleet = getFleet(sender) ?: return

        if (!(isFleetCommand(sender) ?: return)) {
            sender.userError("You are not the commander of this fleet")
        }

        Fleets.delete(fleet)
        sender.success("Disbanded fleet")
    }

    @Subcommand("list")
    @Suppress("unused")
    fun onFleetList(sender: Player) {
        val fleet = getFleet(sender) ?: return

        fleet.list(sender)
    }

    @Subcommand("leave")
    @Suppress("unused")
    fun onFleetLeave(sender: Player) {
        val fleet = getFleet(sender) ?: return

        if (isFleetCommand(sender) ?: return) {
            sender.userError("Transfer command of your fleet before leaving")
            return
        }

        fleet.remove(sender)
    }

    @Subcommand("kick")
    @Suppress("unused")
    @CommandCompletion("@players")
    fun onFleetKick(sender: Player, memberName: String) {
        val fleet = getFleet(sender) ?: return

        if (!(isFleetCommand(sender) ?: return)) {
            sender.userError("You are not the commander of this fleet")
            return
        }

        val player = Bukkit.getPlayer(memberName)
        if (player == null) {
            sender.userError("Player $memberName is not found or not online")
            return
        }

        if (!fleet.get(player)) {
            sender.userError("Player ${player.name} is not in this fleet")
            return
        }

        if (sender.name == memberName) {
            sender.userError("You cannot kick yourself; transfer command of this fleet or delete this fleet (/fleet delete)")
            return
        }

        fleet.remove(player)
        sender.success("Removed ${player.name} from fleet")
    }

    @Subcommand("transfer")
    @Suppress("unused")
    @CommandCompletion("@players")
    fun onFleetTransfer(sender: Player, memberName: String) {
        val fleet = getFleet(sender) ?: return

        if (!(isFleetCommand(sender) ?: return)) {
            sender.userError("You are not the commander of this fleet")
            return
        }

        val player = Bukkit.getPlayer(memberName)
        if (player == null) {
            sender.userError("Player $memberName is not found or not online")
            return
        }

        if (!fleet.get(player)) {
            sender.userError("Player ${player.name} is not in this fleet")
            return
        }

        if (sender.name == memberName) {
            sender.userError("You cannot transfer fleet command to yourself")
            return
        }

        fleet.switchLeader(player)
        sender.success("Switched fleet command to ${player.name}")
    }

    @Subcommand("clearbroadcast|clearbc")
    @Suppress("unused")
    @CommandCompletion("@players")
    fun onFleetClearBroadcast(sender: Player) {
        val fleet = getFleet(sender) ?: return

        if (!(isFleetCommand(sender) ?: return)) {
            sender.userError("You are not the commander of this fleet")
            return
        }

        fleet.lastBroadcast = ""
        sender.success("Cleared broadcast message")
    }

    @Subcommand("broadcast|bc")
    @Suppress("unused")
    @CommandCompletion("@players")
    fun onFleetBroadcast(sender: Player, broadcast: String) {
        val fleet = getFleet(sender) ?: return

        if (!(isFleetCommand(sender) ?: return)) {
            sender.userError("You are not the commander of this fleet")
            return
        }

        fleet.broadcast(broadcast)
        sender.success("Broadcast message \"$broadcast\"")
    }

    private fun getFleet(sender: Player): Fleet? {
        val fleet = Fleets.findByMember(sender)

        if (fleet == null) {
            sender.userError("You are not in a fleet")
            return null
        } else return fleet
    }

    private fun isFleetCommand(sender: Player): Boolean? {
        val fleet = Fleets.findByMember(sender) ?: return null

        return fleet.leaderId == sender.uniqueId
    }
}