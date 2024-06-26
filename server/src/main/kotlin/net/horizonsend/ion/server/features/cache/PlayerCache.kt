package net.horizonsend.ion.server.features.cache

import net.horizonsend.ion.common.database.cache.nations.AbstractPlayerCache
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.server.miscellaneous.utils.actualStyle
import net.horizonsend.ion.server.miscellaneous.utils.listen
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.UUID

object PlayerCache : AbstractPlayerCache() {
	override fun load() {
		super.load()

        listen<PlayerQuitEvent> { event ->
            callOnQuit(event.player.uniqueId)
        }

        listen<AsyncPlayerPreLoginEvent>(priority = EventPriority.MONITOR, ignoreCancelled = true) { event ->
            callOnPreLogin(event.uniqueId)
        }

        listen<PlayerJoinEvent>(priority = EventPriority.LOWEST) { event ->
            callOnLoginLow(event.player.uniqueId)
        }
	}

	override fun getColoredTag(nameColorPair: Pair<String, String>?): String? {
		if (nameColorPair?.first == null)
			return null

		return "${nameColorPair.second.actualStyle.wrappedColor}${nameColorPair.first}"
	}

	override fun onlinePlayerIds(): List<SLPlayerId> =
		Bukkit.getOnlinePlayers().map(Player::slPlayerId)

	override fun kickUUID(uuid: UUID, msg: String) {
		Bukkit.getPlayer(uuid)!!.kick(MiniMessage.miniMessage().deserialize(msg))
	}

	fun getIfOnline(player: Player): PlayerData? = PLAYER_DATA[player.uniqueId]

	operator fun get(player: Player): PlayerData = PLAYER_DATA[player.uniqueId]
		?: error("Data wasn't cached for ${player.name}")
}
