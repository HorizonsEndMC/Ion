package net.horizonsend.ion.server.features.starship.fleet

import net.horizonsend.ion.server.IonServerComponent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerQuitEvent

object Fleets : IonServerComponent() {

    private val fleetList = mutableListOf<Fleet>()

    fun findByMember(player: Player) = fleetList.find { it.get(player) }

    fun create(player: Player) = fleetList.add(Fleet(player.uniqueId))

    fun delete(fleet: Fleet) {
        if (fleetList.contains(fleet)) {
            fleet.delete()
            fleetList.remove(fleet)
        }
    }

    @Suppress("unused")
    @EventHandler
    fun onPlayerLeave(event: PlayerQuitEvent) {
        val player = event.player

        findByMember(player)?.remove(player) ?: return
    }
}