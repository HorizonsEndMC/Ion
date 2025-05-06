package net.horizonsend.ion.proxy.features.cache

import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.connection.LoginEvent
import com.velocitypowered.api.event.player.ServerConnectedEvent
import net.horizonsend.ion.common.database.cache.AbstractPlayerSettingsCache
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.proxy.PLUGIN
import net.horizonsend.ion.proxy.features.cache.PlayerCache.callOnLoginLow
import net.horizonsend.ion.proxy.features.cache.PlayerCache.callOnPreLogin
import net.horizonsend.ion.proxy.features.cache.PlayerCache.callOnQuit
import net.horizonsend.ion.proxy.utils.slPlayerId
import net.kyori.adventure.text.Component
import java.util.concurrent.TimeUnit
import kotlin.jvm.optionals.getOrNull

object PlayerSettingsCache : AbstractPlayerSettingsCache(), Listener {
	override fun runAsync(task: () -> Unit) {
		PLUGIN.proxy.scheduler.async(task)
	}

	override fun kickId(player: SLPlayerId, reason: Component) {
		PLUGIN.server.getPlayer(player.id).getOrNull()?.disconnect(reason)
	}

	override fun load() {}

	@Subscribe(order = PostOrder.LATE)
	fun preLogin(event: LoginEvent) {
		if (PLUGIN.proxy.players.any { it.uniqueId == event.player.uniqueId }) return

		callOnPreLogin(event.player.uniqueId)
	}

	@Subscribe(order = PostOrder.LATE)
	fun login(event: ServerConnectedEvent) {
		if (!isCached(event.player.slPlayerId)) {
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
}
