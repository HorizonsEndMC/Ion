package net.horizonsend.ion.proxy.features.cache

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.player.ServerConnectedEvent
import com.velocitypowered.api.event.player.ServerPreConnectEvent
import com.velocitypowered.api.proxy.Player
import net.horizonsend.ion.common.IonComponent
import net.horizonsend.ion.common.database.SLTextStyleDB
import net.horizonsend.ion.common.database.cache.Cache
import net.horizonsend.ion.common.database.cache.nations.AbstractPlayerCache
import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.common.database.cache.nations.RelationCache
import net.horizonsend.ion.common.database.cache.nations.SettlementCache
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.proxy.IonProxy
import net.horizonsend.ion.proxy.utils.slPlayerId
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import java.time.Duration
import java.util.*
import kotlin.jvm.optionals.getOrNull

object Caches : IonComponent() {
	private val caches: List<Cache> = listOf(
		PlayerCache,
		SettlementCache,
		NationCache,
		RelationCache
	)

	override fun onEnable() = caches.forEach {
		it.load()
		IonProxy.proxy.eventManager.register(IonProxy, it)
	}
}

object PlayerCache : AbstractPlayerCache() {

	@Subscribe
	fun preLogin(e: ServerPreConnectEvent) {
		if (e.player.currentServer.isPresent) return
		callOnPreLogin(e.player.uniqueId)
	}

	@Subscribe
	fun login(e: ServerConnectedEvent) {
		if (!PlayerCache.PLAYER_DATA.containsKey(e.player.uniqueId)) {
			IonProxy.proxy.scheduler.buildTask(IonProxy) {
				callOnPreLogin(e.player.uniqueId)
				callOnLoginLow(e.player.uniqueId)
			}.delay(Duration.ofSeconds(1)).schedule()
		}
	}

	@Subscribe
	fun quit(e: DisconnectEvent) =
		callOnQuit(e.player.uniqueId)

	override fun onlinePlayerIds(): List<SLPlayerId> =
		IonProxy.proxy.allPlayers.map(Player::slPlayerId)

	override fun kickUUID(uuid: UUID, msg: String): Unit =
		IonProxy.proxy.getPlayer(uuid).getOrNull()?.disconnect(miniMessage().deserialize(msg))!!

	override fun getColoredTag(nameColorPair: Pair<String, SLTextStyleDB>?): String? {
		return nameColorPair?.first //TODO: Find an actual solution
	}

	fun getIfOnline(player: Player): PlayerData? = PLAYER_DATA[player.uniqueId]

	operator fun get(player: Player): PlayerData = PLAYER_DATA[player.uniqueId]
		?: error("Data wasn't cached for ${player.username}")
}
