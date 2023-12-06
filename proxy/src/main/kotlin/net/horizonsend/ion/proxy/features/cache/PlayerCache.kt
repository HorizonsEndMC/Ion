package net.horizonsend.ion.proxy.features.cache

import net.horizonsend.ion.common.database.cache.nations.AbstractPlayerCache
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.slPlayerId
import net.horizonsend.ion.proxy.PLUGIN
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.event.PlayerDisconnectEvent
import net.md_5.bungee.api.event.PreLoginEvent
import net.md_5.bungee.api.event.ServerConnectedEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import java.util.UUID
import java.util.concurrent.TimeUnit

object PlayerCache : AbstractPlayerCache(), Listener {
	@EventHandler
	fun preLogin(event: PreLoginEvent) {
		if (PLUGIN.proxy.players.any { it.uniqueId == event.connection.uniqueId }) return

		callOnPreLogin(event.connection.uniqueId)
	}

	@EventHandler
	fun login(event: ServerConnectedEvent) {
		if (!PlayerCache.PLAYER_DATA.containsKey(event.player.uniqueId)) {
			PLUGIN.proxy.scheduler.delay(
				1L,
				TimeUnit.SECONDS
			) {
				callOnPreLogin(event.player.uniqueId)
				callOnLoginLow(event.player.uniqueId)
			}
		}
	}

	@EventHandler
	fun quit(event: PlayerDisconnectEvent) = callOnQuit(event.player.uniqueId)

	override fun onlinePlayerIds(): List<SLPlayerId> = PLUGIN.proxy.players.map { it.uniqueId.slPlayerId }

	override fun kickUUID(uuid: UUID, msg: String) {
		PLUGIN.getProxy().getPlayer(uuid)?.disconnect(*BungeeComponentSerializer.get()
			.serialize(MiniMessage.miniMessage().deserialize(msg)))
	}

	override fun getColoredTag(nameColorPair: Pair<String, String>?): String? {
		return nameColorPair?.first //TODO: Find an actual solution
	}

	fun getIfOnline(player: ProxiedPlayer): PlayerData? = PLAYER_DATA[player.uniqueId]

	operator fun get(player: ProxiedPlayer): PlayerData = PLAYER_DATA[player.uniqueId]
		?: error("Data wasn't cached for ${player.name}")
}
