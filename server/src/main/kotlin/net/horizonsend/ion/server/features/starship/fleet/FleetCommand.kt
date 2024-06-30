package net.horizonsend.ion.server.features.starship.fleet

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.starship.fleet.Fleets.fleet
import org.bukkit.entity.Player

@CommandAlias("fleet")
object FleetCommand : SLCommand() {
    @Subcommand("create")
    @Suppress("unused")
    fun onFleetCreate(sender: Player) {
        if (sender.fleet() != null) {
            sender.userError("You are already in a fleet")
            return
        }

        Fleets.create(sender)
        sender.success("Created fleet")
    }

    @Subcommand("delete")
    @Suppress("unused")
    fun onFleetDelete(sender: Player) {
        val fleet = sender.fleet()

        if (fleet == null) {
            sender.userError("You are not in a fleet")
            return
        }

        if (fleet.leaderId != sender.uniqueId) {
            sender.userError("You are not the commander of this fleet")
        }

        Fleets.delete(fleet)
        sender.success("Deleted fleet")
    }

    @Subcommand("list")
    @Suppress("unused")
    fun onFleetList(sender: Player) {
        val fleet = sender.fleet()

        if (fleet == null) {
            sender.userError("You are not in a fleet")
            return
        }

        fleet.list(sender)
    }
}