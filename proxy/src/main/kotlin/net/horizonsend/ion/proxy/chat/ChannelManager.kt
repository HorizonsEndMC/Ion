package net.horizonsend.ion.proxy.chat

import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.player.PlayerChatEvent
import com.velocitypowered.api.event.player.ServerConnectedEvent
import com.velocitypowered.api.proxy.Player
import net.horizonsend.ion.common.IonComponent
import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.common.database.cache.nations.RelationCache
import net.horizonsend.ion.common.database.cache.nations.SettlementCache
import net.horizonsend.ion.common.extensions.informationAction
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.extensions.userErrorAction
import net.horizonsend.ion.common.miniMessage
import net.horizonsend.ion.common.redis
import net.horizonsend.ion.common.utils.luckPerms
import net.horizonsend.ion.proxy.IonProxy
import net.horizonsend.ion.proxy.chat.channels.*
import net.horizonsend.ion.proxy.features.cache.PlayerCache
import net.horizonsend.ion.proxy.lpHasPermission
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import java.util.*

object ChannelManager : IonComponent() {
	private val global = GlobalChannel()
	private val channels = listOf(
		global,
		PlanetChannel(),
		LocalChannel(),
		SettlementChat(),
		NationChat(),
		AllyChat(),
		StaffChat(),
		ModChat(),
		DevChat(),
		ServerChat(),
		ContentDesignChat(),
		AdminChat()
	)
	private val localCache = Collections.synchronizedMap(mutableMapOf<UUID, Channel>())

	private fun redisKey(playerID: UUID): String = "chat.selectedchannel.$playerID"
	private fun refreshCache(player: Player) =
		redis {
			localCache[player.uniqueId] = get(redisKey(player.uniqueId))?.let { e -> channels.find { e == it.name } }
				?: global
		}

	override fun onEnable() {
		for (player in IonProxy.proxy.allPlayers) {
			refreshCache(player)
		}

		for (channel in channels)
			IonProxy.proxy.commandManager.register(
				channel.commands.first(),

				object : SimpleCommand {
					override fun execute(invocation: SimpleCommand.Invocation) {
						val player = invocation.source() as? Player
						val oldChannel = localCache[player?.uniqueId] ?: global

						if (player?.lpHasPermission("ion.chat.${channel.name.lowercase()}") != true) {
							player?.userError("You don't have access to that chat.")
							return
						}

						if (oldChannel == channel) {
							player.userErrorAction(
								"""
									<red>You're already in chat ${channel.displayName.uppercase(Locale.getDefault())}<red>!" <italic>(Hint: To get back to global, use /global)
									""".trimIndent()
							)

							return
						} else {
							localCache[player.uniqueId] = channel

							player.informationAction(
								"""
									<white>Switched to ${channel.displayName.uppercase(Locale.getDefault())}<white> chat! <white>To switch back to your previous chat, use '/${oldChannel.commands.first()}'
									""".trimIndent()
							)
						}

						IonProxy.proxy.scheduler.buildTask(IonProxy) {
							redis {
								player.uniqueId?.let {
									set(redisKey(it), channel.name)
								}
							}
						}.schedule()
					}
				},

				*channel.commands.toTypedArray()
			)
	}

	@Subscribe
	fun onJoin(e: ServerConnectedEvent) {
		if (e.previousServer.isPresent) return

		refreshCache(e.player)
	}

	@Subscribe
	fun onQuit(e: DisconnectEvent) {
		localCache.remove(e.player.uniqueId)
	}

	@Subscribe
	fun playerChat(e: PlayerChatEvent) {
		val player = e.player
		var channel = localCache[player?.uniqueId] ?: global
		var message = e.message

		if (e.message.startsWith("!")) {
			channel = global
			message = message.removePrefix("!")
		}

		e.result = PlayerChatEvent.ChatResult.denied()

		if (!channel.processMessage(player, e)) return

		val user = luckPerms.userManager.getUser(player.uniqueId)
		val players =
			if (channel.checkPermission)
				channel.receivers(player)
					.filter { it.lpHasPermission("ion.channel.${channel.name.lowercase()}") && it != player }
			else channel.receivers(player).filterNot { it == player }

		val channelPrefixComponent = channel.prefix?.miniMessage()?.append(text(" ")) ?: empty()
		val playerPrefixComponent = user?.cachedData?.metaData?.prefix?.miniMessage() ?: empty()
		val playerSuffixComponent = user?.cachedData?.metaData?.suffix?.miniMessage() ?: empty()

		(players + player).forEach {
			it.sendMessage(
				text().append(
					channelPrefixComponent,
					playerPrefixComponent,

					text("[").color(NamedTextColor.DARK_GRAY),
					text((PlayerCache[player].level ?: 1)).color(NamedTextColor.AQUA),
					text("]").color(NamedTextColor.DARK_GRAY),
					text(" "),
					text(player.username).color(NamedTextColor.WHITE),

					playerSuffixComponent,

					text(" » ").color(NamedTextColor.DARK_GRAY),
				).hoverEvent(playerInfo(player)).append(
					if (player.lpHasPermission("ion.minimessage"))
						message.miniMessage().color(channel.color)
					else
						text(
							MiniMessage.miniMessage()
								.stripTags(message)
						).color(channel.color)
				)
			)
		}
	}

	fun playerInfo(player: Player) = HoverEvent.showText(
		text(
			"""
			Level: ${PlayerCache[player].level}
			XP: ${PlayerCache[player].xp}
			Nation: ${PlayerCache[player].nationOid?.let(NationCache::get)?.name}
			Settlement: ${PlayerCache[player].settlementOid?.let(SettlementCache::get)?.name}
			Player: ${player.username}
			""".trimIndent()
		)
	)
}
