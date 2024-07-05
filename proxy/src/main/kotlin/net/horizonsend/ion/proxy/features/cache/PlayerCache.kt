package net.horizonsend.ion.proxy.features.cache

import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.connection.LoginEvent
import com.velocitypowered.api.event.player.ServerConnectedEvent
import com.velocitypowered.api.proxy.Player
import net.horizonsend.ion.common.database.cache.nations.AbstractPlayerCache
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.slPlayerId
import net.horizonsend.ion.proxy.PLUGIN
import net.kyori.adventure.text.minimessage.MiniMessage
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.jvm.optionals.getOrNull

object PlayerCache : AbstractPlayerCache(), Listener {
	@Subscribe(order = PostOrder.LATE)
	fun preLogin(event: LoginEvent) {
		if (PLUGIN.proxy.players.any { it.uniqueId == event.player.uniqueId }) return

		callOnPreLogin(event.player.uniqueId)
	}

	@Subscribe(order = PostOrder.LATE)
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

	@Subscribe
	fun quit(event: DisconnectEvent) = callOnQuit(event.player.uniqueId)

	override fun onlinePlayerIds(): List<SLPlayerId> = PLUGIN.proxy.players.map { it.uniqueId.slPlayerId }

	override fun kickUUID(uuid: UUID, msg: String) {
		PLUGIN.server.getPlayer(uuid).getOrNull()?.disconnect(MiniMessage.miniMessage().deserialize(msg))
	}

	override fun getColoredTag(nameColorPair: Pair<String, String>?): String? {
		return nameColorPair?.first //TODO: Find an actual solution
	}

	fun getIfOnline(player: Player): PlayerData? = PLAYER_DATA[player.uniqueId]
	fun getIfOnline(player: UUID): PlayerData? = PLAYER_DATA[player]

	operator fun get(player: Player): PlayerData = PLAYER_DATA[player.uniqueId]
		?: error("Data wasn't cached for ${player.username}")
}
