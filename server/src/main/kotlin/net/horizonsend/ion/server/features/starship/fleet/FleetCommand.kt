package net.horizonsend.ion.server.features.starship.fleet

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.command.SLCommand
import org.bukkit.Bukkit
import org.bukkit.entity.Player

@CommandAlias("fleet|f")
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

        fleet.userError("Your Fleet Commander has disbanded your fleet!")
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
        fleet.information("${sender.name} has left your fleet")
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
        player.userError("You were kicked from ${sender.name}'s fleet!")
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

    @Subcommand("invite")
    @Suppress("unused")
    @CommandCompletion("@players")
    fun onFleetInvite(sender: Player, inviteName: String) {
        val fleet = getFleet(sender) ?: return

        if (!(isFleetCommand(sender) ?: return)) {
            sender.userError("You are not the commander of this fleet")
            return
        }

        val player = Bukkit.getPlayer(inviteName)
        if (player == null) {
            sender.userError("Player $inviteName is not found or not online")
            return
        }

        if (fleet.get(player)) {
            sender.userError("Player ${player.name} is already in this fleet")
            return
        }

        fleet.invite(player)
        player.information("You have been invited to join ${sender.name}'s fleet. Enter \"/fleet join ${sender.name}\" " +
                "to join their fleet.")
        sender.success("Invited ${player.name} to your fleet")
    }

    @Subcommand("removeInvite")
    @Suppress("unused")
    @CommandCompletion("@players")
    fun onFleetRemoveInvite(sender: Player, inviteName: String) {
        val fleet = getFleet(sender) ?: return

        if (!(isFleetCommand(sender) ?: return)) {
            sender.userError("You are not the commander of this fleet")
            return
        }

        val player = Bukkit.getPlayer(inviteName)
        if (player == null) {
            sender.userError("Player $inviteName is not found or not online")
            return
        }

        if (fleet.get(player)) {
            sender.userError("Player ${player.name} is already in this fleet")
            return
        }

        if (!fleet.getInvite(player)) {
            sender.userError("Player ${player.name} has not been invited")
        }

        fleet.removeInvite(player)
        player.userError("Your invite to ${sender.name}'s fleet has been removed.")
        sender.success("Removed fleet invite from ${player.name}")
    }

    @Subcommand("join")
    @Suppress("unused")
    @CommandCompletion("@players")
    fun onFleetJoin(sender: Player, inviterName: String) {
        if (Fleets.findByMember(sender) != null) {
            sender.userError("You are already in a fleet")
            return
        }

        val fleetInvites = Fleets.findInvitesByMember(sender)
        if (fleetInvites.isEmpty()) {
            sender.userError("You have no pending fleet invites")
            return
        }

        for (fleet in fleetInvites) {
            val inviter = Bukkit.getPlayer(inviterName) ?: continue

            if (fleet.leaderId == inviter.uniqueId) {
                fleet.information(("${sender.name} has joined your fleet"))
                fleet.add(sender)
                fleet.removeInvite(sender)
                sender.success("Joined ${inviter.name}'s fleet")
                return
            }
        }

        // player failed to join a fleet they are invited to
        sender.userError("You have no invites from $inviterName, or the Fleet Commander is not found")
    }

    @Subcommand("clearbroadcast|clearbc")
    @Suppress("unused")
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

    @Subcommand("jump")
    @Suppress("unused")
    @Description("Jump fleet to a set of coordinates, a hyperspace beacon, or a planet")
    fun onFleetJump(sender: Player) {
        val fleet = getFleet(sender) ?: return

        if (!(isFleetCommand(sender) ?: return)) {
            sender.userError("You are not the commander of this fleet")
            return
        }

        fleet.information("Fleet Commander issuing fleet jump command")
        fleet.jumpFleet()
        sender.success("Jumping fleet")
    }

    @Subcommand("jump")
    @Suppress("unused")
    @CommandCompletion("x|z")
    @Description("Jump fleet to a set of coordinates, a hyperspace beacon, or a planet")
    fun onFleetJump(sender: Player, xCoordinate: String, zCoordinate: String) {
        val fleet = getFleet(sender) ?: return

        if (!(isFleetCommand(sender) ?: return)) {
            sender.userError("You are not the commander of this fleet")
            return
        }

        fleet.information("Fleet Commander issuing fleet jump command")
        fleet.jumpFleet(xCoordinate.toInt(), zCoordinate.toInt())
        sender.success("Jumping fleet")
    }

    @Subcommand("jump")
    @Suppress("unused")
    @CommandCompletion("auto|@planetsInWorld|@hyperspaceGatesInWorld")
    @Description("Jump fleet to a set of coordinates, a hyperspace beacon, or a planet")
    fun onFleetJump(sender: Player, destination: String) {
        val fleet = getFleet(sender) ?: return

        if (!(isFleetCommand(sender) ?: return)) {
            sender.userError("You are not the commander of this fleet")
            return
        }

        fleet.information("Fleet Commander issuing fleet jump command")
        fleet.jumpFleet(destination)
        sender.success("Jumping fleet")
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