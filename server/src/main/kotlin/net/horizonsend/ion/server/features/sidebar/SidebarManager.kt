package net.horizonsend.ion.server.features.sidebar

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServer.scoreboardLibrary
import net.horizonsend.ion.server.miscellaneous.runnable
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.Collections
import java.util.UUID

class SidebarManager : Listener {
    companion object {
        val sidebar = scoreboardLibrary.createSidebar()
        val playerSidebars: MutableMap<UUID, MainSidebar> = Collections.synchronizedMap(mutableMapOf<UUID, MainSidebar>())
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        sidebar.addPlayer(event.player)
        playerSidebars[event.player.uniqueId] = MainSidebar(event.player, sidebar)
    }

    @EventHandler
    fun onPlayerLeave(event: PlayerQuitEvent) {
        sidebar.removePlayer(event.player)
        playerSidebars.remove(event.player.uniqueId)
    }

    init {
        runnable {
            IonServer.server.onlinePlayers.forEach {
                playerSidebars[it.uniqueId]?.tick()
            }
        }.runTaskTimer(IonServer, 0L, 1L)
    }
}