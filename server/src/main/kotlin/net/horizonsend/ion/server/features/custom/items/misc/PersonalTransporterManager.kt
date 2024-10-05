package net.horizonsend.ion.server.features.custom.items.misc

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.features.custom.items.CustomItems
import net.horizonsend.ion.server.listener.SLEventListener
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerQuitEvent
import java.util.UUID

object PersonalTransporterManager : SLEventListener() {
    // key = requester, value = target
    private val inviteList = mutableMapOf<UUID, UUID>()

    fun addTpRequest(requester: Player, target: Player) {
        if (requester.uniqueId == target.uniqueId) {
            requester.userError("You cannot teleport to yourself!")
            return
        }

        val existingTargetId = inviteList[requester.uniqueId]
        if (existingTargetId != null) {
            requester.userError("You already have a pending teleport request!")
            return
        }

        inviteList[requester.uniqueId] = target.uniqueId
        requester.information("Sent ${target.name} a teleport request (expires in 120 seconds)")
        requester.sendRichMessage(
            "<gold><italic><hover:show_text:'<gray>/personaltransporter cancel'><click:run_command:/personaltransporter cancel>[Cancel]</click>"
        )
        target.information("${requester.name} has sent a request to teleport to you (expires in 120 seconds)")
        target.sendRichMessage(
            "<green><italic><hover:show_text:'<gray>/personaltransporter accept'><click:run_command:/personaltransporter accept ${requester.name}>[Accept]</click> " +
                    "<red><italic><hover:show_text:'<gray>/personaltransporter deny'><click:run_command:/personaltransporter deny ${requester.name}>[Deny]</click>"
        )

        Tasks.syncDelay(20L * 120) {
            removeTpRequest(requester)
        }
    }

    fun removeTpRequest(requester: Player) {
        val targetId = inviteList[requester.uniqueId]
        if (targetId != null) {
            val target = Bukkit.getPlayer(targetId)
            if (target != null) {
                requester.information("Teleport request to ${target.name} was removed.")
                target.information("Teleport request from ${requester.name} was removed.")
            } else {
                requester.information("Teleport request to an offline player was removed.")
            }
            inviteList.remove(requester.uniqueId)
        }
    }

    fun acceptTpRequest(requester: Player, target: Player) {
        removeItemFromPlayer(requester)
        requester.teleport(target)
        removeTpRequest(requester)
    }

    fun checkItemFromPlayer(target: Player): Boolean {
        return target.inventory.contains(CustomItems.PERSONAL_TRANSPORTER.constructItemStack())
    }

    fun removeItemFromPlayer(target: Player) {
        if (checkItemFromPlayer(target)) {
            target.inventory.removeItemAnySlot(CustomItems.PERSONAL_TRANSPORTER.constructItemStack())
        }
    }

    fun checkTpRequestExists(requester: Player, target: Player): Boolean {
        return inviteList[requester.uniqueId] == target.uniqueId
    }

    private fun getRequestsToPlayer(target: Player): Set<UUID> {
        return inviteList.filterValues { it == target.uniqueId }.keys
    }

    @Suppress("unused")
    @EventHandler
    fun onPlayerLeave(event: PlayerQuitEvent) {
        val player = event.player

        // Remove requests made by the player
        removeTpRequest(player)
        // Remove requests targeting the player
        for (requesterId in getRequestsToPlayer(player)) {
            val requester = Bukkit.getPlayer(requesterId)
            if (requester != null) {
                removeTpRequest(requester)
            }
        }
    }
}