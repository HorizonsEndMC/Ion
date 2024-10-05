package net.horizonsend.ion.server.command.qol

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.custom.items.misc.PersonalTransporterManager
import org.bukkit.Bukkit
import org.bukkit.entity.Player

@CommandAlias("personaltransporter")
object PersonalTransporterCommand : SLCommand() {
    @Suppress("unused")
    @Subcommand("cancel")
    fun onCancelTeleportRequest(sender: Player) {
        PersonalTransporterManager.removeTpRequest(sender)
        sender.success("Removed teleport request (if there was one)")
    }

    @Suppress("unused")
    @Subcommand("accept")
    fun onAcceptTeleportRequest(sender: Player, requesterName: String) {
        val requester = Bukkit.getPlayer(requesterName)

        if (requester == null || !PersonalTransporterManager.checkTpRequestExists(requester, sender))
        {
            sender.userError("No teleport request found from $requesterName")
            return
        } else if (!PersonalTransporterManager.checkItemFromPlayer(requester)) {
            sender.userError("$requesterName does not have a Personal Transporter")
            requester.userError("${sender.name} accepted your request, but you do not have a Personal Transporter")
            return
        } else {
            PersonalTransporterManager.acceptTpRequest(requester, sender)
            sender.success("Accepted teleport request from $requesterName")
            requester.success("Teleported to ${sender.name}")
        }
    }

    @Suppress("unused")
    @Subcommand("deny")
    fun onDenyTeleportRequest(sender: Player, requesterName: String) {
        val requester = Bukkit.getPlayer(requesterName)

        if (requester == null || !PersonalTransporterManager.checkTpRequestExists(requester, sender))
        {
            sender.userError("No teleport request found from $requesterName")
            return
        } else {
            PersonalTransporterManager.removeTpRequest(requester)
            sender.success("Removed teleport request from $requesterName")
            requester.userError("${sender.name} denied your teleport request")
        }
    }
}