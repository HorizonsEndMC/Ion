package net.horizonsend.ion.server.features.chat

import github.scarsz.discordsrv.DiscordSRV
import io.papermc.paper.event.player.AsyncChatEvent
import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.common.database.cache.nations.RelationCache
import net.horizonsend.ion.common.database.cache.nations.SettlementCache
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.schema.nations.NationRelation
import net.horizonsend.ion.common.database.schema.nations.Settlement
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.extensions.userErrorAction
import net.horizonsend.ion.common.utils.redis.RedisAction
import net.horizonsend.ion.common.utils.text.addSpace
import net.horizonsend.ion.common.utils.text.bracketed
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.LegacySettings
import net.horizonsend.ion.server.command.misc.GToggleCommand
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.chat.messages.NationsChatMessage
import net.horizonsend.ion.server.features.chat.messages.NormalChatMessage
import net.horizonsend.ion.server.features.progression.Levels
import net.horizonsend.ion.server.features.progression.SLXP
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.miscellaneous.utils.PlayerWrapper.Companion.common
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.AQUA
import net.kyori.adventure.text.format.NamedTextColor.BLUE
import net.kyori.adventure.text.format.NamedTextColor.DARK_AQUA
import net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY
import net.kyori.adventure.text.format.NamedTextColor.DARK_GREEN
import net.kyori.adventure.text.format.NamedTextColor.DARK_PURPLE
import net.kyori.adventure.text.format.NamedTextColor.DARK_RED
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.LIGHT_PURPLE
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.luckperms.api.node.NodeEqualityPredicate
import org.bukkit.Bukkit
import org.bukkit.entity.Player

@Suppress("UNUSED") // They're used
enum class ChatChannel(val displayName: Component, val commandAliases: List<String>, val messageColor: TextColor) {
	GLOBAL(text("Global", DARK_GREEN), listOf("global", "g"), WHITE) {
		override fun onChat(player: Player, event: AsyncChatEvent) {
			if (LegacySettings.chat.noGlobalWorlds.contains(player.world.name)) {
				player.userErrorAction("You can't use global chat in this world! <italic>(If you need assistance, please use /msg)")
			}

			if (GToggleCommand.noGlobalInheritanceNode != null) {
				val user = player.common().getUser()

				if (user.data().contains(GToggleCommand.noGlobalInheritanceNode, NodeEqualityPredicate.IGNORE_EXPIRY_TIME).asBoolean()) {
					player.userErrorAction("You have gtoggle on! Use /gtoggle to disable.")
					return
				}
			}

			val component = formatChatMessage(empty(), player, event, messageColor)
			globalAction(component)

			try {
				discord(event)
			} catch (e: ClassNotFoundException) {
				// ignore, plugin just isn't loaded
			} catch (e: NoClassDefFoundError) {
				// ignore
			}
		}

		private fun discord(event: AsyncChatEvent) {
			DiscordSRV.getPlugin().processChatMessage(
				event.player,
				event.message().plainText(),
				null,
				false
			)
		}
	},

	LOCAL(text("Local", YELLOW), listOf("local", "l"), YELLOW) {
		private val distanceSquared = LegacySettings.chat.localDistance * LegacySettings.chat.localDistance

		override fun onChat(player: Player, event: AsyncChatEvent) {
			val component = formatChatMessage(text("Local", YELLOW, TextDecoration.BOLD), player, event, messageColor).buildChatComponent()

			for (other in player.world.players) {
				if (other.location.distanceSquared(player.location) > distanceSquared) continue

				other.sendMessage(component)
			}
		}
	},

	PLANET(text("Planet", BLUE), listOf("planetchat", "pchat", "pc"), GREEN) {
		override fun onChat(player: Player, event: AsyncChatEvent) {
			val world = player.world

			if (Space.getPlanet(world) == null) {
				return player.userError("You're not on a planet! To go back to global chat, use /global")
			}

			val component = formatChatMessage(text("Planet", GREEN, TextDecoration.BOLD), player, event, messageColor).buildChatComponent()

			for (other in player.world.players) {
				other.sendMessage(component)
			}
		}
	},

	ADMIN(text("Admin", RED), listOf("admin", "adminchat"), RED) {
		override fun onChat(player: Player, event: AsyncChatEvent) {
			if (!player.hasPermission("chat.channel.admin")) {
				player.userError("You don't have access to that!")
				return
			}

			adminAction(formatChatMessage(text("Admin", DARK_RED, TextDecoration.BOLD), player, event, messageColor))
		}
	},

	STAFF(text("Staff", AQUA), listOf("staff", "staffchat"), LIGHT_PURPLE) {
		override fun onChat(player: Player, event: AsyncChatEvent) {
			if (!player.hasPermission("chat.channel.staff")) {
				player.userError("You don't have access to that!")
				return
			}

			staffAction(formatChatMessage(text("Staff", DARK_GRAY, TextDecoration.BOLD), player, event, messageColor))
		}
	},

	MOD(text("Mod", GREEN), listOf("mod", "modchat"), AQUA) {
		override fun onChat(player: Player, event: AsyncChatEvent) {
			if (!player.hasPermission("chat.channel.mod")) {
				player.userError("You don't have access to that!")
				return
			}

			modAction(formatChatMessage(text("Mod", DARK_AQUA, TextDecoration.BOLD), player, event, messageColor))
		}
	},

	DEV(text("Dev", GREEN), listOf("dev", "devchat"), GREEN) {
		override fun onChat(player: Player, event: AsyncChatEvent) {
			if (!player.hasPermission("chat.channel.dev")) {
				player.userError("You don't have access to that!")
				return
			}

			devAction(formatChatMessage(text("Dev", DARK_AQUA, TextDecoration.BOLD), player, event, messageColor))
		}
	},

	ContentDesign(ofChildren(text("Content", GREEN), text("Design", RED)), listOf("contentdesign", "cd", "slcd"), NamedTextColor.GOLD) {
		override fun onChat(player: Player, event: AsyncChatEvent) {
			if (!player.hasPermission("chat.channel.contentdesign")) {
				player.userError("You don't have access to that!")
				return
			}

			contentDesignAction(formatChatMessage(
				ofChildren(text("Content", GREEN, TextDecoration.BOLD), text("Design", RED, TextDecoration.BOLD)),
				player,
				event,
				messageColor
			))
		}
	},

	VIP(text("VIP", GREEN), listOf("vip", "vipchat"), DARK_GREEN) {
		override fun onChat(player: Player, event: AsyncChatEvent) {
			if (!player.hasPermission("chat.channel.vip")) {
				player.userError("You don't have access to that!")
				return
			}

			vipAction(formatChatMessage(text("VIP", GREEN, TextDecoration.BOLD), player, event, messageColor))
		}
	},

	CREW(text("Crew", HEColorScheme.HE_LIGHT_BLUE), listOf("crew", "c"), HEColorScheme.HE_LIGHT_BLUE) {
		override fun onChat(player: Player, event: AsyncChatEvent) {
			val starship = ActiveStarships.findByPassenger(player)
				?: return player.userError("You're not riding a starship! <italic>(Hint: To get back to global, use /global)")

			val prefix = when (player) {
				(starship.controller as? PlayerController)?.player -> text("[Captain]", BLUE)
				else -> text("[Ship]", HEColorScheme.HE_LIGHT_BLUE)
			}

			starship.sendMessage(formatChatMessage(prefix, player, event, messageColor).buildChatComponent())
		}
	},

	SETTLEMENT(text("Settlement", DARK_AQUA, TextDecoration.BOLD), listOf("schat", "sc", "settlementchat"), AQUA) {
		override fun onChat(player: Player, event: AsyncChatEvent) {
			val playerData = PlayerCache[player]
			val settlement = playerData.settlementOid ?: return player.userError("You're not in a settlement! <italic>(Hint: To get back to global, use /global)")

			val prefix = ofChildren(
				displayName,
				LegacyComponentSerializer.legacyAmpersand().deserialize(playerData.settlementTag ?: "").addSpace(true)
			)

			settlementAction(NationsChatMessage(
				id = settlement,
				prefix = prefix,
				playerPrefix = player.common().getPrefix(),
				playerDisplayName = event.player.displayName(),
				playerSuffix = player.common().getSuffix(),
				message = event.message(),
				playerInfo = text(playerInfo(player)),
				color = messageColor
			))
		}
	},

	NATION(text("Nation", GREEN, TextDecoration.BOLD), listOf("nchat", "nc", "nationchat"), GREEN) {
		override fun onChat(player: Player, event: AsyncChatEvent) {
			val playerData = PlayerCache[player]
			val settlement = playerData.settlementOid ?: return player.userError("You're not in a settlement! <italic>(Hint: To get back to global, use /global)")
			val nation = playerData.nationOid ?: return player.userError("You're not in a nation! <italic>(Hint: To get back to global, use /global")

			val settlementName = SettlementCache[settlement].name

			val prefix = ofChildren(
				displayName,
				text(" "),
				text(settlementName, AQUA),
				LegacyComponentSerializer.legacyAmpersand().deserialize(playerData.nationTag ?: "").addSpace(true)
			)

			nationAction(NationsChatMessage(
				id = nation,
				prefix = prefix,
				playerPrefix = player.common().getPrefix(),
				playerDisplayName = event.player.displayName(),
				playerSuffix = player.common().getSuffix(),
				message = event.message(),
				playerInfo = text(playerInfo(player)),
				color = messageColor
			))
		}
	},

	ALLY(text("Ally", DARK_PURPLE, TextDecoration.BOLD), listOf("achat", "ac", "allychat"), LIGHT_PURPLE) {
		override fun onChat(player: Player, event: AsyncChatEvent) {
			val playerData = PlayerCache[player]
			val nation = playerData.nationOid ?: return player.userError("You're not in a nation! <italic>(Hint: To get back to global, use /global)")

			val nationName = NationCache[nation].name

			val prefix = ofChildren(
				displayName,
				text(" "),
				text(nationName, YELLOW),
				LegacyComponentSerializer.legacyAmpersand().deserialize(playerData.nationTag ?: "").addSpace(true)
			)

			allyAction(NationsChatMessage(
				id = nation,
				prefix = prefix,
				playerPrefix = player.common().getPrefix(),
				playerDisplayName = event.player.displayName(),
				playerSuffix = player.common().getSuffix(),
				message = event.message(),
				playerInfo = text(playerInfo(player)),
				color = messageColor
			))
		}
	};

	abstract fun onChat(player: Player, event: AsyncChatEvent)

	companion object ChannelActions : IonServerComponent() {
		private val globalAction = { message: NormalChatMessage ->
			val component = message.buildChatComponent()

			for (player in Bukkit.getOnlinePlayers()) {
				if (LegacySettings.chat.noGlobalWorlds.contains(player.world.name)) continue

				if (GToggleCommand.noGlobalInheritanceNode != null) {
					val user = player.common().getUser()

					if (user.data().contains(GToggleCommand.noGlobalInheritanceNode, NodeEqualityPredicate.IGNORE_EXPIRY_TIME).asBoolean()) {
						continue
					}
				}
				player.sendMessage(component)
			}
		}.registerRedisAction("chat-global", runSync = false)

		private fun simpleCrossServerChannelAction(name: String): RedisAction<NormalChatMessage> {
			return { message: NormalChatMessage ->
				val component = message.buildChatComponent()

				for (player in Bukkit.getOnlinePlayers()) {
					if (player.hasPermission("chat.channel.$name")) player.sendMessage(component)
				}
			}.registerRedisAction("chat-$name", runSync = false)
		}

		private val adminAction = simpleCrossServerChannelAction("admin")

		// Keeping this as a relic
		private val pumpkinAction = simpleCrossServerChannelAction("pumpkin")

		private val staffAction = simpleCrossServerChannelAction("staff")
		private val modAction = simpleCrossServerChannelAction("mod")
		private val devAction = simpleCrossServerChannelAction("dev")
		private val contentDesignAction = simpleCrossServerChannelAction("contentdesign")
		private val vipAction = simpleCrossServerChannelAction("vip")

		private val settlementAction = { message: NationsChatMessage<Settlement> ->
			val component = message.buildChatComponent()
			for (player in Bukkit.getOnlinePlayers()) {
				if (player.isOnline && PlayerCache.getIfOnline(player)?.settlementOid == message.id) {
					player.sendMessage(component)
				}
			}
		}.registerRedisAction("nations-chat-msg-settlement", runSync = false)

		private val nationAction = { message: NationsChatMessage<Nation> ->
			val component = message.buildChatComponent()
			for (player in Bukkit.getOnlinePlayers()) {
				if (PlayerCache.getIfOnline(player)?.nationOid == message.id) {
					player.sendMessage(component)
				}
			}
		}.registerRedisAction("nations-chat-msg-nation", runSync = false)

		private val allyAction = { message: NationsChatMessage<Nation> ->
			val component = message.buildChatComponent()
			for (player in Bukkit.getOnlinePlayers()) {
				val playerNation = PlayerCache.getIfOnline(player)?.nationOid ?: continue

				if (RelationCache[playerNation, message.id] >= NationRelation.Level.ALLY) {
					player.sendMessage(component)
				}
			}
		}.registerRedisAction("nations-chat-msg-ally", runSync = false)
	}

	fun formatChatMessage(
		prefix: Component,
		player: Player,
		event: AsyncChatEvent,
		color: TextColor
	): NormalChatMessage = NormalChatMessage(
		prefix = ofChildren(bracketed(text(Levels[event.player], AQUA)), prefix.addSpace(true)),
		playerPrefix = player.common().getPrefix(),
		playerDisplayName = event.player.displayName(),
		playerSuffix = player.common().getSuffix(),
		message = event.message(),
		playerInfo = text(playerInfo(player)),
		color = color
	)
}

private fun playerInfo(player: Player): String =
	"""
	Level: ${Levels[player]}
	XP: ${SLXP[player]}
	Nation: ${PlayerCache[player].nationOid?.let(NationCache::get)?.name}
	Settlement: ${PlayerCache[player].settlementOid?.let(SettlementCache::get)?.name}
	Player: ${player.name}
	""".trimIndent()
