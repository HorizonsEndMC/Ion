package net.horizonsend.ion.server.features.sidebar

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.megavex.scoreboardlibrary.api.ScoreboardLibrary
import net.megavex.scoreboardlibrary.api.exception.NoPacketAdapterAvailableException
import net.megavex.scoreboardlibrary.api.noop.NoopScoreboardLibrary
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.kyori.adventure.key.Key
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*

object Sidebar : IonServerComponent() {
	val fontKey = Key.key("horizonsend:sidebar")

	private val playerSidebars: MutableMap<UUID, MainSidebar> = Collections.synchronizedMap(mutableMapOf<UUID, MainSidebar>())
	private val scoreboardLibrary by lazy {
		try {
			ScoreboardLibrary.loadScoreboardLibrary(IonServer)
		} catch (e: NoPacketAdapterAvailableException) {
			NoopScoreboardLibrary()
		}
	}

	override fun onEnable() {
		Tasks.syncRepeat(0L, 2L) {
			Bukkit.getOnlinePlayers().forEach {
				playerSidebars[it.uniqueId]?.tick()
			}
		}
	}

	override fun onDisable() {
		scoreboardLibrary.close()
	}

	@EventHandler
	fun onPlayerJoin(event: PlayerJoinEvent) {
		val sidebar = scoreboardLibrary.createSidebar()
		sidebar.addPlayer(event.player)
		playerSidebars[event.player.uniqueId] = MainSidebar(event.player, sidebar)
	}

	@EventHandler
	fun onPlayerLeave(event: PlayerQuitEvent) {
		val sidebar = playerSidebars[event.player.uniqueId] ?: return
		sidebar.backingSidebar.removePlayer(event.player)
		sidebar.backingSidebar.close()
		playerSidebars.remove(event.player.uniqueId)
	}
}
