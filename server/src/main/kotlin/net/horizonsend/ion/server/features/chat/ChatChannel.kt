package net.horizonsend.ion.server.features.chat

import github.scarsz.discordsrv.DiscordSRV
import net.horizonsend.ion.common.database.DbObject
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.common.database.cache.nations.RelationCache
import net.horizonsend.ion.common.database.cache.nations.SettlementCache
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.schema.nations.NationRelation
import net.horizonsend.ion.common.database.schema.nations.Settlement
import net.horizonsend.ion.common.extensions.userErrorAction
import net.horizonsend.ion.common.utils.luckPerms
import net.horizonsend.ion.common.utils.redisaction.RedisAction
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.LegacySettings
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.nations.utils.hover
import net.horizonsend.ion.server.features.progression.Levels
import net.horizonsend.ion.server.features.progression.SLXP
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.miscellaneous.utils.SLTextStyle
import net.horizonsend.ion.server.miscellaneous.utils.colorize
import net.horizonsend.ion.server.miscellaneous.utils.msg
import net.luckperms.api.node.NodeEqualityPredicate
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.player.AsyncPlayerChatEvent

enum class ChatChannel(val displayName: String, val commandAliases: List<String>, val messageColor: SLTextStyle) {
	GLOBAL("<dark_green>Global", listOf("global", "g"), SLTextStyle.RESET) {
		override fun onChat(player: Player, event: AsyncPlayerChatEvent) {
			if (LegacySettings.chat.noGlobalWorlds.contains(player.world.name)) {
				player.userErrorAction(
					"You can't use global chat in this world! " +
						"<italic>(If you need assistance, please use /msg)"
				)
			}

			val group = luckPerms.groupManager.getGroup("noglobal")

			if (group != null) {
				val node = luckPerms.nodeBuilderRegistry.forInheritance().group(group).value(true).build()
				val user = luckPerms.userManager.getUser(player.uniqueId)
				if (user?.data()?.contains(node, NodeEqualityPredicate.IGNORE_EXPIRY_TIME)?.asBoolean() == true) {
					player.userErrorAction("You have gtoggle on! Use /gtoggle to disable.")
					return
				}
			}

			val prefix = event.format.format(player.displayName, "")
			val message = "$messageColor${event.message}"
			val playerInfo = playerInfo(player)
			globalAction(NormalChatMessage(prefix, message, playerInfo))
			try {
				discord(event)
			} catch (e: ClassNotFoundException) {
				// ignore, plugin just isn't loaded
			}
		}

		private fun discord(event: AsyncPlayerChatEvent) {
			DiscordSRV.getPlugin().processChatMessage(event.player, event.message, null, false)
		}
	},
	LOCAL("<yellow>Local", listOf("local", "l"), SLTextStyle.YELLOW) {
		private val distanceSquared = LegacySettings.chat.localDistance * LegacySettings.chat.localDistance

		override fun onChat(player: Player, event: AsyncPlayerChatEvent) {
			val prefix = "&e&lLocal".colorize() + " " + event.format.format(player.displayName, "")
			val message = "$messageColor${event.message}"
			val playerInfo = playerInfo(player)
			val component = NormalChatMessage(prefix, message, playerInfo).buildChatComponent()
			for (other in player.world.players) {
				if (other.location.distanceSquared(player.location) <= distanceSquared) {
					other.sendMessage(*component)
				}
			}
		}
	},
	PLANET("<blue>Planet", listOf("planetchat", "pchat", "pc"), SLTextStyle.DARK_GREEN) {
		override fun onChat(player: Player, event: AsyncPlayerChatEvent) {
			val world = player.world

			if (Space.getPlanet(world) == null) {
				return player msg "&cYou're not on a planet! To go back to global chat, use /global"
			}

			val prefix = "&9&lPlanet".colorize() + " " + event.format.format(player.displayName, "")
			val message = "$messageColor${event.message}"
			val playerInfo = playerInfo(player)

			val component = NormalChatMessage(prefix, message, playerInfo).buildChatComponent()

			for (other in player.world.players) {
				other.sendMessage(*component)
			}
		}
	},
	ADMIN("<red>Admin", listOf("admin", "adminchat"), SLTextStyle.RED) {
		override fun onChat(player: Player, event: AsyncPlayerChatEvent) {
			if (!player.hasPermission("chat.channel.admin")) {
				player msg "&cYou don't have access to that!"
				return
			}

			val prefix = "&4&lAdmin&r ".colorize() + event.format.format(player.displayName, "")
			val message = "$messageColor${event.message}"
			val playerInfo = playerInfo(player)
			adminAction(NormalChatMessage(prefix, message, playerInfo))
		}
	},
	STAFF("<aqua>Staff", listOf("staff", "staffchat"), SLTextStyle.LIGHT_PURPLE) {
		override fun onChat(player: Player, event: AsyncPlayerChatEvent) {
			if (!player.hasPermission("chat.channel.staff")) {
				player msg "&cYou don't have access to that!"
				return
			}

			val prefix = "&8&lStaff&r ".colorize() + event.format.format(player.displayName, "")
			val message = "$messageColor${event.message}".colorize()
			val playerInfo = playerInfo(player)

			staffAction(NormalChatMessage(prefix, message, playerInfo))
		}
	},
	MOD("<green>Mod", listOf("mod", "modchat"), SLTextStyle.AQUA) {
		override fun onChat(player: Player, event: AsyncPlayerChatEvent) {
			if (!player.hasPermission("chat.channel.mod")) {
				player msg "&cYou don't have access to that!"
				return
			}

			val prefix = "&3&lMod&r ".colorize() + event.format.format(player.displayName, "")
			val message = "$messageColor${event.message}".colorize()
			val playerInfo = playerInfo(player)

			modAction(NormalChatMessage(prefix, message, playerInfo))
		}
	},
	DEV("<green>dev", listOf("dev", "devchat"), SLTextStyle.GREEN) {
		override fun onChat(player: Player, event: AsyncPlayerChatEvent) {
			if (!player.hasPermission("chat.channel.dev")) {
				player msg "&cYou don't have access to that!"
				return
			}

			val prefix = "&3&ldev&r ".colorize() + event.format.format(player.displayName, "")
			val message = "$messageColor${event.message}".colorize()
			val playerInfo = playerInfo(player)

			devAction(NormalChatMessage(prefix, message, playerInfo))
		}
	},
	ContentDesign("<green>Content <red>Design", listOf("contentdesign", "cd", "slcd"), SLTextStyle.GOLD) {
		override fun onChat(player: Player, event: AsyncPlayerChatEvent) {
			if (!player.hasPermission("chat.channel.contentdesign")) {
				player msg "&cYou don't have access to that!"
				return
			}

			val prefix = "&a&lContent &c&lDesign&r ".colorize() + event.format.format(player.displayName, "")
			val message = "$messageColor${event.message}".colorize()
			val playerInfo = playerInfo(player)

			contentDesignAction(NormalChatMessage(prefix, message, playerInfo))
		}
	},
	VIP("<green>VIP", listOf("vip", "vipchat"), SLTextStyle.DARK_GREEN) {
		override fun onChat(player: Player, event: AsyncPlayerChatEvent) {
			if (!player.hasPermission("chat.channel.vip")) {
				player msg "&cYou don't have access to that!"
				return
			}

			val prefix = "&2&lVIP&r ".colorize() + event.format.format(player.displayName, "")
			val message = "$messageColor${event.message}".colorize()
			val playerInfo = playerInfo(player)

			vipAction(NormalChatMessage(prefix, message, playerInfo))
		}
	}, /*
	CREW("&bCrew", listOf("crew", "c"), SLTextStyle.ITALIC) {
		override fun onChat(player: Player, event: AsyncPlayerChatEvent) {
			val starship = StarshipManager.getRiding(player)
				?: return player msg "&cYou're not riding a starship! &o(Hint: To get back to global, use /global)"

			val message = (when (player) {
				starship.pilot -> "${SLTextStyle.DARK_PURPLE}[Captain]${SLTextStyle.BLUE} "
				else -> "${SLTextStyle.GRAY}"
			}) + player.name + " ${SLTextStyle.RESET}$messageColor" + event.message

			starship.messagePassengersgit(message)
		}
	},*/ // TODO: Add this back after merging starships
	SETTLEMENT("<dark_aqua>Settlement", listOf("schat", "sc", "settlementchat"), SLTextStyle.AQUA) {
		override fun onChat(player: Player, event: AsyncPlayerChatEvent) {
			val playerData = PlayerCache[player]
			val settlement = playerData.settlementOid
				?: return player msg "&cYou're not in a settlement! &o(Hint: To get back to global, use /global)"

			val roleString = playerData.settlementTag?.let { " $it" } ?: ""

			val prefix = "&3&lSettlement$roleString &b${player.name} &8» ".colorize()
			val message = messageColor.toString() + event.message.replace("${SLTextStyle.RESET}", "$messageColor")

			settlementAction(NationsChatMessage(settlement, prefix, message, playerInfo(player)))
		}
	},
	NATION("<green>Nation", listOf("nchat", "nc", "nationchat"), SLTextStyle.GREEN) {
		override fun onChat(player: Player, event: AsyncPlayerChatEvent) {
			val playerData = PlayerCache[player]
			val settlement = playerData.settlementOid
				?: return player msg "&cYou're not in a settlement! &o(Hint: To get back to global, use /global)"
			val nation = playerData.nationOid
				?: return player msg "&cYou're not in a nation! &o(Hint: To get back to global, use /global)"

			val settlementName = SettlementCache[settlement].name
			val roleString = playerData.nationTag?.let { " $it" } ?: ""

			val prefix = "&a&lNation &b$settlementName$roleString &c${player.name} &8»".colorize()
			val message = messageColor.toString() + event.message.replace("${SLTextStyle.RESET}", "$messageColor")

			nationAction(NationsChatMessage(nation, prefix, message, playerInfo(player)))
		}
	},
	ALLY("<dark_purple>Ally", listOf("achat", "ac", "allychat"), SLTextStyle.LIGHT_PURPLE) {
		override fun onChat(player: Player, event: AsyncPlayerChatEvent) {
			val playerData = PlayerCache[player]
			val nation = playerData.nationOid
				?: return player msg "&cYou're not in a nation! &o(Hint: To get back to global, use /global)"

			val nationName = NationCache[nation].name
			val roleString = playerData.nationTag?.let { " $it" } ?: ""

			val format = "&5&lAlly &e$nationName$roleString &b${player.name} &8»".colorize()
			val message = messageColor.toString() + event.message.replace("${SLTextStyle.RESET}", "$messageColor")

			allyAction(NationsChatMessage(nation, format, message, playerInfo(player)))
		}
	};

	abstract fun onChat(player: Player, event: AsyncPlayerChatEvent)

	companion object ChannelActions : IonServerComponent() {
		private val globalAction = { message: NormalChatMessage ->
			val component = message.buildChatComponent()

			val node = luckPerms.groupManager.getGroup("noglobal")?.let {
				luckPerms.nodeBuilderRegistry.forInheritance().group(it).value(true).build()
			}

			for (player in Bukkit.getOnlinePlayers()) {
				if (LegacySettings.chat.noGlobalWorlds.contains(player.world.name)) {
					continue
				}
				if (node != null) {
					val user = luckPerms.userManager.getUser(player.uniqueId)
					if (user?.data()?.contains(node, NodeEqualityPredicate.IGNORE_EXPIRY_TIME)?.asBoolean() == true) {
						continue
					}
				}
				player.sendMessage(*component)
			}
		}.registerRedisAction("chat-global", runSync = false)

		private fun simpleCrossServerChannelAction(name: String): RedisAction<NormalChatMessage> {
			return { message: NormalChatMessage ->
				val component = message.buildChatComponent()
				for (player in Bukkit.getOnlinePlayers()) {
					if (player.hasPermission("chat.channel.$name")) {
						player.sendMessage(*component)
					}
				}
			}.registerRedisAction("chat-$name", runSync = false)
		}

		private val adminAction = simpleCrossServerChannelAction("admin")
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
					player.sendMessage(*component)
				}
			}
		}.registerRedisAction("nations-chat-msg-settlement", runSync = false)

		private val nationAction = { message: NationsChatMessage<Nation> ->
			val component = message.buildChatComponent()
			for (player in Bukkit.getOnlinePlayers()) {
				if (PlayerCache.getIfOnline(player)?.nationOid == message.id) {
					player.sendMessage(*component)
				}
			}
		}.registerRedisAction("nations-chat-msg-nation", runSync = false)

		private val allyAction = { message: NationsChatMessage<Nation> ->
			val component = message.buildChatComponent()
			for (player in Bukkit.getOnlinePlayers()) {
				val playerNation = PlayerCache.getIfOnline(player)?.nationOid ?: continue

				if (RelationCache[playerNation, message.id] >= NationRelation.Level.ALLY) {
					player.sendMessage(*component)
				}
			}
		}.registerRedisAction("nations-chat-msg-ally", runSync = false)
	}
}

private fun playerInfo(player: Player): String =
	"""
	Level: ${Levels[player]}
	XP: ${SLXP[player]}
	Nation: ${PlayerCache[player].nationOid?.let(NationCache::get)?.name}
	Settlement: ${PlayerCache[player].settlementOid?.let(SettlementCache::get)?.name}
	Player: ${player.name}
	""".trimIndent()

private abstract class ChatMessage {
	abstract val prefix: String
	abstract val message: String
	abstract val playerInfo: String

	fun buildChatComponent(): Array<out BaseComponent> {
		val builder = ComponentBuilder()
		builder.append(TextComponent(prefix.trimEnd()).hover(playerInfo))
		builder.append(TextComponent.fromLegacyText(" " + message.trimStart()))
		return builder.create()
	}
}

private data class NormalChatMessage(
	override val prefix: String,
	override val message: String,
	override val playerInfo: String
) : ChatMessage()

private data class NationsChatMessage<A : DbObject>(
	val id: Oid<A>,
	override val prefix: String,
	override val message: String,
	override val playerInfo: String
) : ChatMessage()
