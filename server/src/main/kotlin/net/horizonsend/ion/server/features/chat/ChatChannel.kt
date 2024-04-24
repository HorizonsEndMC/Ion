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
import net.horizonsend.ion.common.redis.RedisAction
import net.horizonsend.ion.common.utils.text.bracketed
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.formatNationName
import net.horizonsend.ion.common.utils.text.formatSettlementName
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.orEmpty
import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.command.misc.GToggleCommand
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.chat.messages.NationsChatMessage
import net.horizonsend.ion.server.features.chat.messages.NormalChatMessage
import net.horizonsend.ion.server.features.progression.Levels
import net.horizonsend.ion.server.features.progression.SLXP
import net.horizonsend.ion.server.features.sidebar.Sidebar
import net.horizonsend.ion.server.features.sidebar.SidebarIcon
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.fleet.Fleets
import net.horizonsend.ion.server.miscellaneous.utils.CommonPlayerWrapper.Companion.common
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.space
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.AQUA
import net.kyori.adventure.text.format.NamedTextColor.BLUE
import net.kyori.adventure.text.format.NamedTextColor.DARK_AQUA
import net.kyori.adventure.text.format.NamedTextColor.DARK_GREEN
import net.kyori.adventure.text.format.NamedTextColor.DARK_PURPLE
import net.kyori.adventure.text.format.NamedTextColor.DARK_RED
import net.kyori.adventure.text.format.NamedTextColor.GOLD
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.LIGHT_PURPLE
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration.BOLD
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.luckperms.api.node.NodeEqualityPredicate
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.entity.Player

@Suppress("UNUSED") // They're used
enum class ChatChannel(
	val displayName: Component,
	val channelPrefix: Component,
	val shortenedChannelPrefix: Component,
	val commandAliases: List<String>,
	val messageColor: TextColor
) {
	GLOBAL(
		displayName = text("Global", DARK_GREEN),
		channelPrefix = empty(),
		shortenedChannelPrefix = empty(),
		commandAliases = listOf("global", "g"),
		messageColor = WHITE
	) {
		override fun onChat(player: Player, event: AsyncChatEvent) {
			if (ConfigurationFiles.legacySettings().chat.noGlobalWorlds.contains(player.world.name)) {
				player.userErrorAction("You can't use global chat in this world! <italic>(If you need assistance, please use /msg)")
			}

			if (GToggleCommand.noGlobalInheritanceNode != null) {
				val user = player.common().getUser()

				if (user.data().contains(GToggleCommand.noGlobalInheritanceNode, NodeEqualityPredicate.IGNORE_EXPIRY_TIME).asBoolean()) {
					player.userErrorAction("You have gtoggle on! Use /gtoggle to disable.")
					return
				}
			}

			globalAction(formatChatMessage(
				channel = this,
				event = event,
				channelColor = messageColor
			))
//			ServerDiscordMessaging.globalMessage(component.buildChatComponent())

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

	LOCAL(
		displayName = text("Local", YELLOW),
		channelPrefix = text("Local", YELLOW, BOLD),
		shortenedChannelPrefix = bracketed(text("L", YELLOW, BOLD)),
		commandAliases = listOf("local", "l"),
		messageColor = YELLOW
	) {
		private val distanceSquared = ConfigurationFiles.legacySettings().chat.localDistance * ConfigurationFiles.legacySettings().chat.localDistance

		override fun onChat(player: Player, event: AsyncChatEvent) {
			val message = formatChatMessage(
				channel = this,
				event = event,
				channelColor = messageColor
			)

			for (other in player.world.players) {
				if (other.location.distanceSquared(player.location) > distanceSquared) continue
				val cached = PlayerCache.getIfOnline(other) ?: continue
				if (cached.blockedPlayerIDs.contains(player.slPlayerId)) continue

				other.sendMessage(message.buildChatComponent(
					useChannelPrefix = true,
					useLevelsPrefix = true,
					useShortenedPrefix = cached.shortenChatChannels,
					showLuckPermsPrefix = !cached.hideGlobalPrefixes
				))
			}
		}
	},

	PLANET(
		displayName = text("Planet", BLUE),
		channelPrefix = text("Planet", BLUE, BOLD),
		shortenedChannelPrefix = bracketed(text("P", BLUE, BOLD)),
		commandAliases = listOf("planetchat", "pchat", "pc"),
		messageColor = DARK_GREEN
	) {
		override fun onChat(player: Player, event: AsyncChatEvent) {
			val world = player.world

			if (Space.getPlanet(world) == null) {
				return player.userError("You're not on a planet! To go back to global chat, use /global")
			}

			val message = formatChatMessage(this, event, messageColor)

			for (other in player.world.players) {
				val cached = PlayerCache.getIfOnline(other) ?: continue
				if (cached.blockedPlayerIDs.contains(player.slPlayerId)) continue

				other.sendMessage(message.buildChatComponent(
					useLevelsPrefix = true,
					useChannelPrefix = true,
					useShortenedPrefix = cached.shortenChatChannels,
					showLuckPermsPrefix = !cached.hideGlobalPrefixes
				))
			}
		}
	},

	SYSTEM(
		displayName = text("System", TextColor.fromHexString("#FF8234")!!),
		channelPrefix = text("System", TextColor.fromHexString("#FF8234")!!, BOLD),
		shortenedChannelPrefix = bracketed(text("Sy", TextColor.fromHexString("#FF8234")!!, BOLD)),
		commandAliases = listOf("systemchat", "system", "sy"),
		messageColor = TextColor.fromHexString("#FF8234")!!
	) {
		override fun onChat(player: Player, event: AsyncChatEvent) {
			val world = player.world

			val worlds = mutableSetOf<World>()

			worlds.add(world)

			val planet = Space.getPlanet(world)

			if (planet != null) {
				val spaceWorld = planet.spaceWorld
				spaceWorld?.let { worlds.add(it) }

				Space.getAllPlanets().filter { it.spaceWorld?.uid == spaceWorld?.uid }.mapNotNullTo(worlds) { it.planetWorld }
			}

			val spaceWorld = planet?.spaceWorld?.uid ?: world.uid
			Space.getAllPlanets().filter { it.spaceWorld?.uid == spaceWorld }.mapNotNullTo(worlds) { it.planetWorld }
			Space.getMoons().filter { it.spaceWorld?.uid == spaceWorld }.mapNotNullTo(worlds) { it.planetWorld }

			if (worlds.isEmpty()) {
				return player.userError("You're not in a system! To go back to global chat, use /global")
			}

			val message = formatChatMessage(this, event, messageColor)

			for (other in worlds.flatMap { it.players }) {
				if (PlayerCache[other].blockedPlayerIDs.contains(player.slPlayerId)) continue

				other.sendMessage(message.buildChatComponent(
					useLevelsPrefix = true,
					useChannelPrefix = true,
					useShortenedPrefix = PlayerCache[other].shortenChatChannels,
					showLuckPermsPrefix = !PlayerCache[player].hideGlobalPrefixes
				))
			}
		}
	},

	ADMIN(
		displayName = text("Admin", RED),
		channelPrefix = text("Admin", DARK_RED, BOLD),
		shortenedChannelPrefix = bracketed(text("A", DARK_RED, BOLD)),
		commandAliases = listOf("admin", "adminchat"),
		messageColor = RED
	) {
		override fun onChat(player: Player, event: AsyncChatEvent) {
			if (!player.hasPermission("chat.channel.admin")) {
				player.userError("You don't have access to that!")
				return
			}

			adminAction(formatChatMessage(this, event, messageColor))
		}
	},

	STAFF(
		displayName = text("Staff", AQUA),
		channelPrefix = text("Staff", AQUA, BOLD),
		shortenedChannelPrefix = bracketed(text("S", AQUA, BOLD)),
		commandAliases = listOf("staff", "staffchat"),
		messageColor = BLUE
	) {
		override fun onChat(player: Player, event: AsyncChatEvent) {
			if (!player.hasPermission("chat.channel.staff")) {
				player.userError("You don't have access to that!")
				return
			}

			staffAction(formatChatMessage(this, event, messageColor))
		}
	},

	MOD(
		displayName = text("Mod", DARK_AQUA),
		channelPrefix = text("Mod", DARK_AQUA, BOLD),
		shortenedChannelPrefix = bracketed(text("M", DARK_AQUA, BOLD)),
		commandAliases = listOf("mod", "modchat"),
		messageColor = AQUA
	) {
		override fun onChat(player: Player, event: AsyncChatEvent) {
			if (!player.hasPermission("chat.channel.mod")) {
				player.userError("You don't have access to that!")
				return
			}

			modAction(formatChatMessage(this, event, messageColor))
		}
	},

	DEV(
		displayName = text("Dev", GREEN),
		channelPrefix = text("Dev", DARK_AQUA, BOLD),
		shortenedChannelPrefix = bracketed(text("D", DARK_AQUA, BOLD)),
		commandAliases = listOf("dev", "devchat"),
		messageColor = GREEN
	) {
		override fun onChat(player: Player, event: AsyncChatEvent) {
			if (!player.hasPermission("chat.channel.dev")) {
				player.userError("You don't have access to that!")
				return
			}

			devAction(formatChatMessage(this, event, messageColor))
		}
	},

	ContentDesign(
		displayName = ofChildren(text("Content", GREEN), text("Design", RED)),
		channelPrefix = ofChildren(text("Content ", GREEN, BOLD), text("Design ", RED, BOLD)),
		shortenedChannelPrefix = bracketed(ofChildren(text("C", GREEN, BOLD), text("D", RED, BOLD))),
		commandAliases = listOf("contentdesign", "cd", "slcd"),
		messageColor = GOLD
	) {
		override fun onChat(player: Player, event: AsyncChatEvent) {
			if (!player.hasPermission("chat.channel.contentdesign")) {
				player.userError("You don't have access to that!")
				return
			}

			contentDesignAction(formatChatMessage(
				this,
				event,
				messageColor
			))
		}
	},

	VIP(
		displayName = text("VIP", GREEN),
		channelPrefix = text("VIP", GREEN, BOLD),
		shortenedChannelPrefix = text("VIP", GREEN, BOLD),
		commandAliases = listOf("vip", "vipchat"),
		messageColor = DARK_GREEN
	) {
		override fun onChat(player: Player, event: AsyncChatEvent) {
			if (!player.hasPermission("chat.channel.vip")) {
				player.userError("You don't have access to that!")
				return
			}

			vipAction(formatChatMessage(this, event, messageColor))
		}
	},

	CREW(
		displayName = text("Crew", HEColorScheme.HE_LIGHT_BLUE),
		channelPrefix = empty(),
		shortenedChannelPrefix = empty(),
		commandAliases = listOf("crew", "c"),
		messageColor = HEColorScheme.HE_LIGHT_BLUE
	) {
		override fun onChat(player: Player, event: AsyncChatEvent) {
			val starship = ActiveStarships.findByPassenger(player)
				?: return player.userError("You're not riding a starship! <italic>(Hint: To get back to global, use /global)")

			val prefix = when (player) {
				(starship.controller as? PlayerController)?.player -> bracketed(text("Captain", BLUE))
				else -> bracketed(text("Ship", HEColorScheme.HE_LIGHT_BLUE))
			}
			val message = formatChatMessage(this, event, messageColor).buildChatComponent(
				useLevelsPrefix = false,
				useChannelPrefix = false,
				useShortenedPrefix = false,
				showLuckPermsPrefix = !PlayerCache[player].hideGlobalPrefixes
			)

			for (passenger in starship.onlinePassengers) {
				passenger.sendMessage(ofChildren(prefix, message))
			}
		}
	},

	FLEET(
		displayName = text("Fleet", HEColorScheme.HE_DARK_ORANGE),
		channelPrefix = text("Fleet", HEColorScheme.HE_DARK_ORANGE, BOLD, BOLD),
		shortenedChannelPrefix = bracketed(text("F", HEColorScheme.HE_DARK_ORANGE, BOLD)),
		commandAliases = listOf("fleetchat", "fc", "fchat"),
		messageColor = HEColorScheme.HE_LIGHT_ORANGE
	) {
		val leaderPrefix = ofChildren(text(SidebarIcon.FLEET_COMMANDER_ICON.text, GOLD, BOLD).font(Sidebar.fontKey), space())

		override fun onChat(player: Player, event: AsyncChatEvent) {
			val fleet = Fleets.findByMember(player) ?: return player.userError("You're not in a fleet! <italic>(Hint: To get back to global, use /global)")

			val message = formatChatMessage(this, event, messageColor)

			for (fleetMember in fleet.memberIds) {
				val other = Bukkit.getPlayer(fleetMember)!!

				other.sendMessage(message.buildChatComponent(
					useLevelsPrefix = false,
					useChannelPrefix = true,
					useShortenedPrefix = PlayerCache[other].shortenChatChannels,
					additionalPrefix = leaderPrefix.takeIf { component -> fleet.leaderId == player.uniqueId },
					showLuckPermsPrefix = !PlayerCache[player].hideGlobalPrefixes
				))
			}
		}
	},

	SETTLEMENT(
		displayName = text("Settlement", DARK_AQUA),
		channelPrefix = text("Settlement", DARK_AQUA, BOLD),
		shortenedChannelPrefix = bracketed(text("S", DARK_AQUA, BOLD)),
		commandAliases = listOf("schat", "sc", "settlementchat"),
		messageColor = AQUA
	) {
		override fun onChat(player: Player, event: AsyncChatEvent) {
			val playerData = PlayerCache[player]
			val settlement = playerData.settlementOid ?: return player.userError("You're not in a settlement! <italic>(Hint: To get back to global, use /global)")
			val cached = SettlementCache[settlement]

			settlementAction(NationsChatMessage(
				senderLevel = Levels[player],
				channel = this,
				nationsid = settlement,
				settlementName = formatSettlementName(settlement),
				nationName = cached.nation?.let { formatNationName(it) }.orEmpty(),
				nationsRole = LegacyComponentSerializer.legacyAmpersand().deserialize(playerData.nationTag ?: ""),
				settlementRole = LegacyComponentSerializer.legacyAmpersand().deserialize(playerData.settlementTag ?: ""),
				luckPermsPrefix = player.common().getPrefix(),
				playerDisplayName = event.player.displayName(),
				luckPermsSuffix = player.common().getSuffix(),
				message = event.message(),
				playerInfo = text(playerInfo(player)),
				color = messageColor
			))
		}
	},

	NATION(
		displayName = text("Nation", GREEN),
		channelPrefix = text("Nation", GREEN, BOLD),
		shortenedChannelPrefix = bracketed(text("N", GREEN, BOLD)),
		commandAliases = listOf("nchat", "nc", "nationchat"),
		messageColor = GREEN
	) {
		override fun onChat(player: Player, event: AsyncChatEvent) {
			val playerData = PlayerCache[player]
			val settlement = playerData.settlementOid ?: return player.userError("You're not in a settlement! <italic>(Hint: To get back to global, use /global)")
			val nation = playerData.nationOid ?: return player.userError("You're not in a nation! <italic>(Hint: To get back to global, use /global")

			nationAction(NationsChatMessage(
				senderLevel = Levels[player],
				channel = this,
				nationsid = nation,
				settlementName = formatSettlementName(settlement),
				nationName = formatNationName(nation),
				nationsRole = LegacyComponentSerializer.legacyAmpersand().deserialize(playerData.nationTag ?: ""),
				settlementRole = LegacyComponentSerializer.legacyAmpersand().deserialize(playerData.settlementTag ?: ""),
				luckPermsPrefix = player.common().getPrefix(),
				playerDisplayName = event.player.displayName(),
				luckPermsSuffix = player.common().getSuffix(),
				message = event.message(),
				playerInfo = text(playerInfo(player)),
				color = messageColor
			))
		}
	},

	ALLY(
		displayName = text("Ally", DARK_PURPLE),
		channelPrefix = text("Ally", DARK_PURPLE, BOLD),
		shortenedChannelPrefix = bracketed(text("A", DARK_PURPLE, BOLD)),
		commandAliases = listOf("achat", "ac", "allychat"),
		messageColor = LIGHT_PURPLE
	) {
		override fun onChat(player: Player, event: AsyncChatEvent) {
			val playerData = PlayerCache[player]
			val settlement = playerData.settlementOid ?: return player.userError("You're not in a settlement! <italic>(Hint: To get back to global, use /global)")
			val nation = playerData.nationOid ?: return player.userError("You're not in a nation! <italic>(Hint: To get back to global, use /global)")

			allyAction(NationsChatMessage(
				senderLevel = Levels[player],
				channel = this,
				nationsid = nation,
				settlementName = formatSettlementName(settlement),
				nationName = formatNationName(nation),
				nationsRole = LegacyComponentSerializer.legacyAmpersand().deserialize(playerData.nationTag ?: ""),
				settlementRole = LegacyComponentSerializer.legacyAmpersand().deserialize(playerData.settlementTag ?: ""),
				luckPermsPrefix = player.common().getPrefix(),
				playerDisplayName = event.player.displayName(),
				luckPermsSuffix = player.common().getSuffix(),
				message = event.message(),
				playerInfo = text(playerInfo(player)),
				color = messageColor
			))
		}
	};

	abstract fun onChat(player: Player, event: AsyncChatEvent)

	companion object ChannelActions : IonServerComponent() {
		private val globalAction = { message: NormalChatMessage ->
			val sender = message.sender

			for (player in Bukkit.getOnlinePlayers()) {
				if (ConfigurationFiles.legacySettings().chat.noGlobalWorlds.contains(player.world.name)) continue
				if (PlayerCache[player].blockedPlayerIDs.contains(sender)) continue

				if (GToggleCommand.noGlobalInheritanceNode != null) {
					val user = player.common().getUser()

					if (user.data().contains(GToggleCommand.noGlobalInheritanceNode, NodeEqualityPredicate.IGNORE_EXPIRY_TIME).asBoolean()) {
						continue
					}
				}

				player.sendMessage(message.buildChatComponent(
					useLevelsPrefix = true,
					useChannelPrefix = false,
					useShortenedPrefix = false,
					showLuckPermsPrefix = !PlayerCache[player].hideGlobalPrefixes
				))
			}
		}.registerRedisAction("chat-global", runSync = false)

		private fun simpleCrossServerChannelAction(name: String): RedisAction<NormalChatMessage> = { message: NormalChatMessage ->
			for (player in Bukkit.getOnlinePlayers()) {
				if (!player.hasPermission("chat.channel.$name")) continue

				player.sendMessage(message.buildChatComponent(
					useLevelsPrefix = false,
					useChannelPrefix = true,
					useShortenedPrefix = PlayerCache[player].shortenChatChannels,
					showLuckPermsPrefix = !PlayerCache[player].hideUserPrefixes
				))
			}
		}.registerRedisAction("chat-$name", runSync = false)


		private val adminAction = simpleCrossServerChannelAction("admin")

		// Keeping this as a relic
		private val pumpkinAction = simpleCrossServerChannelAction("pumpkin")

		private val staffAction = simpleCrossServerChannelAction("staff")
		private val modAction = simpleCrossServerChannelAction("mod")
		private val devAction = simpleCrossServerChannelAction("dev")
		private val contentDesignAction = simpleCrossServerChannelAction("contentdesign")
		private val vipAction = simpleCrossServerChannelAction("vip")

		private val settlementAction = { message: NationsChatMessage<Settlement> ->
			for (player in Bukkit.getOnlinePlayers()) {
				val cached = PlayerCache.getIfOnline(player)
				if (cached == null || cached.settlementOid != message.nationsid) continue

				player.sendMessage(message.buildChatComponent(
					useLevelsPrefix = false,
					useChannelPrefix = true,
					useShortenedPrefix = cached.shortenChatChannels,
					showSettlementNamePrefix = false,
					showSettlementRolePrefix = !PlayerCache[player].hideUserPrefixes,
					showNationNamePrefix = false,
					showNationRolePrefix = false,
					showLuckPermsPrefix = !PlayerCache[player].hideUserPrefixes
				))
			}
		}.registerRedisAction("nations-chat-msg-settlement", runSync = false)

		private val nationAction = { message: NationsChatMessage<Nation> ->
			for (player in Bukkit.getOnlinePlayers()) {
				if (PlayerCache.getIfOnline(player)?.nationOid != message.nationsid) continue

				player.sendMessage(message.buildChatComponent(
					useLevelsPrefix = false,
					useChannelPrefix = true,
					useShortenedPrefix = PlayerCache[player].shortenChatChannels,
					showSettlementNamePrefix = true,
					showSettlementRolePrefix = false,
					showNationNamePrefix = false,
					showNationRolePrefix = !PlayerCache[player].hideUserPrefixes,
					showLuckPermsPrefix = !PlayerCache[player].hideUserPrefixes
				))
			}
		}.registerRedisAction("nations-chat-msg-nation", runSync = false)

		private val allyAction = { message: NationsChatMessage<Nation> ->
			for (player in Bukkit.getOnlinePlayers()) {
				val playerNation = PlayerCache.getIfOnline(player)?.nationOid ?: continue
				if (RelationCache[playerNation, message.nationsid] < NationRelation.Level.ALLY) continue

				player.sendMessage(message.buildChatComponent(
					useLevelsPrefix = false,
					useChannelPrefix = true,
					useShortenedPrefix = PlayerCache[player].shortenChatChannels,
					showSettlementNamePrefix = false,
					showSettlementRolePrefix = false,
					showNationNamePrefix = true,
					showNationRolePrefix = !PlayerCache[player].hideUserPrefixes,
					showLuckPermsPrefix = !PlayerCache[player].hideUserPrefixes
				))
			}
		}.registerRedisAction("nations-chat-msg-ally", runSync = false)
	}

	fun formatChatMessage(
		channel: ChatChannel,
		event: AsyncChatEvent,
		channelColor: TextColor
	): NormalChatMessage = NormalChatMessage(
		senderLevel = Levels[event.player],
		channel = channel,
		luckPermsPrefix = event.player.common().getPrefix(),
		playerDisplayName = event.player.displayName(),
		luckPermsSuffix = event.player.common().getSuffix(),
		message = event.message(),
		playerInfo = text(playerInfo(event.player)),
		color = channelColor,
		sender = event.player.slPlayerId
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
