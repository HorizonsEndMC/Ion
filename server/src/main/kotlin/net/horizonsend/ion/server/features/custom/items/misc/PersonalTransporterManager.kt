package net.horizonsend.ion.server.features.custom.items.misc

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerQuitEvent
import java.util.UUID

object PersonalTransporterManager : IonServerComponent() {
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
        requester.information("Sent ${target.name} a teleport request")
        target.information("${requester.name} has sent a request to teleport to you.")

        Tasks.syncDelay(20L * 120) {
            removeTpRequest(requester)
        }
    }

    fun removeTpRequest(requester: Player) {
        if (inviteList.containsKey(requester.uniqueId)) {
            inviteList.remove(requester.uniqueId)
        }
    }

    fun getRequestsToPlayer(target: Player): Set<UUID> {
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