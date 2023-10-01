package net.horizonsend.ion.server.features.chat

import net.horizonsend.ion.common.extensions.informationAction
import net.horizonsend.ion.common.extensions.userErrorAction
import net.horizonsend.ion.common.redis
import net.horizonsend.ion.common.utils.muteCache
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.enumValueOfOrNull
import net.horizonsend.ion.server.miscellaneous.utils.listen
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.Collections
import java.util.Locale
import java.util.UUID

object ChannelSelections : IonServerComponent() {
	private val localCache = Collections.synchronizedMap(mutableMapOf<UUID, ChatChannel>())

	private fun redisKey(playerID: UUID): String = "chat.selectedchannel.$playerID"

	private fun refreshCache(playerId: UUID) {
		redis {
			localCache[playerId] = get(
				redisKey(
					playerId
				)
			)?.let { enumValueOfOrNull<ChatChannel>(it) }
				?: ChatChannel.GLOBAL
		}
	}

	override fun onEnable() {
		listen<AsyncPlayerPreLoginEvent>(EventPriority.MONITOR, ignoreCancelled = true) { event ->
			refreshCache(event.uniqueId)
		}

		listen<PlayerJoinEvent> { event ->
			if (!localCache.containsKey(event.player.uniqueId)) {
				refreshCache(event.player.uniqueId)
			}
		}

		listen<PlayerQuitEvent> { event ->
			localCache.remove(event.player.uniqueId)
		}

		for (player in Bukkit.getOnlinePlayers()) {
			refreshCache(player.uniqueId)
		}

		listen<PlayerCommandPreprocessEvent> { event ->
			val player: Player = event.player

			val message: String = event.message
			val args: List<String> = message.removePrefix("/").split(" ")
			val command: String = args[0].toLowerCase()

			ChatChannel.values().firstOrNull { it.commandAliases.contains(command) }?.let { channel ->
				event.isCancelled = true

				val playerID: UUID = player.uniqueId

				val oldChannel = get(player)

				if (args.size > 1) {
					if (muteCache[playerID]) return@let

					localCache[playerID] = channel
					try {
						player.chat(message.removePrefix("/").removePrefix("$command "))
					} finally {
						localCache[playerID] = oldChannel
					}
					return@let
				}

				if (oldChannel == channel) {
					player.userErrorAction(
						"<red>You're already in chat ${channel.displayName.uppercase(Locale.getDefault())}<red>! " +
							"<italic>(Hint: To get back to global, use /global)"
					)
					return@listen
				} else {
					localCache[playerID] = channel
					val info: String =
						"<white><bold>Switched to ${channel.displayName.uppercase(Locale.getDefault())}<white><bold> chat! " +
							"<white><bold>To switch back to your previous chat, use '/${oldChannel.commandAliases.first()}'"
					player.informationAction(info)
				}

				Tasks.async {
					redis {
						set(redisKey(playerID), channel.name)
						return@redis
					}
				}
			}
		}
	}

	operator fun get(player: Player): ChatChannel = localCache[player.uniqueId] ?: ChatChannel.GLOBAL
}
