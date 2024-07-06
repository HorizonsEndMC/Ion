package net.horizonsend.ion.proxy.features.tablist

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.player.ServerConnectedEvent
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.player.TabList
import com.velocitypowered.api.proxy.player.TabListEntry
import com.velocitypowered.api.proxy.server.RegisteredServer
import com.velocitypowered.api.scheduler.ScheduledTask
import com.velocitypowered.api.util.GameProfile
import net.horizonsend.ion.common.utils.configuration.redis
import net.horizonsend.ion.common.utils.text.bracketed
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.proxy.IonProxyComponent
import net.horizonsend.ion.proxy.PLUGIN
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.AQUA
import net.kyori.adventure.text.format.NamedTextColor.DARK_RED
import net.kyori.adventure.text.format.NamedTextColor.GOLD
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
import net.kyori.adventure.text.format.TextColor
import java.time.Duration
import java.util.UUID
import kotlin.math.ceil

object TabList : IonProxyComponent() {
	private lateinit var playerTickTask: ScheduledTask

	override fun onEnable() {
		playerTickTask = PLUGIN.server.scheduler.buildTask(PLUGIN, ::tickPlayers)
			.repeat(Duration.ofSeconds(1))
			.schedule()
	}

	override fun onDisable() {
		playerTickTask.cancel()
	}

	private fun tickPlayers() {
		for (server in PLUGIN.server.allServers) {
			val header = buildHeader(server.serverInfo.name)
			val tps = formatTps(server)

			for (player in server.playersConnected) {
				val footer = buildFooter(player, tps)
				player.sendPlayerListHeaderAndFooter(header, footer)
			}
		}
	}

	private fun buildHeader(serverName: String): Component {
		return ofChildren(
			newline(),
			text("Welcome to Horizon's End!", YELLOW), newline(),
			newline(),
			text("Map: ", AQUA), text("$serverName.horizonsend.net", GRAY), newline(),
			text("Wiki: ", AQUA), text("wiki.horizonsend.net", GRAY), newline(),
			text("Discord: ", AQUA), text("discord.horizonsend.net", GRAY), newline(),
			empty()
		)
	}

	private fun buildFooter(player: Player, tps: Component): Component {
		val seperator = text(" | ", HEColorScheme.HE_MEDIUM_GRAY)

		return ofChildren(
			newline(),
			formatPing(player.ping), seperator, formatOnline(), seperator, tps,
			newline(),
			empty()
		)
	}

	private fun formatPing(ping: Long): Component {
		val color: TextColor = when (ping) {
			in -1..75 -> GREEN
			in 76..150 -> YELLOW
			in 151..300 -> GOLD
			in 301..600 -> RED
			in 601..Long.MAX_VALUE -> DARK_RED
			else -> WHITE
		}

		return ofChildren(text(ping, color), text("ms", GRAY))
	}

	private fun formatOnline(): Component {
		return ofChildren(text(PLUGIN.server.playerCount), text(" Online", AQUA))
	}

	private fun formatTps(server: RegisteredServer): Component {
		val tps = redis {
			val key = "tps_${server.serverInfo.name}"

			if (!exists(key)) return@redis 0

			get(key).toInt()
		}

		val color: TextColor = when (tps) {
			in 1..10 -> DARK_RED
			in 10..15 -> RED
			in 16..17 -> GOLD
			in 18..19 -> YELLOW
			in 20..Int.MAX_VALUE -> GREEN
			else -> WHITE
		}

		return ofChildren(text(tps, color), text(" Server TPS", AQUA))
	}

	// This event should handle initial connections and server changes
	@Subscribe
	fun onPlayerLogin(event: ServerConnectedEvent) {
		PLUGIN.server.scheduler.buildTask(PLUGIN, ::rebuildTabLists)
			.delay(Duration.ofSeconds(1))
			.schedule()
	}

	@Subscribe
	fun onPlayerDisconnect(event: DisconnectEvent) {
		PLUGIN.server.scheduler.buildTask(PLUGIN, ::rebuildTabLists)
			.delay(Duration.ofSeconds(1))
			.schedule()
	}

	private fun rebuildTabLists() {
		PLUGIN.server.allPlayers.forEach { rebuildTabList(it.tabList) }
	}

	private fun rebuildTabList(tabList: TabList) {
		val serverHeights = PLUGIN.server.allServers.associateWith { getSectionHeight(it) }
		val columns = PLUGIN.tabListConfiguration.columns
		val totalHeight = getTotalHeight(serverHeights)

		println("total height: $totalHeight")
		println("columns: $columns")

		val entries: Array<Array<TabListEntry?>> = Array(totalHeight) { arrayOfNulls(columns) }

		var verticalIndex = 0

		for (server in PLUGIN.server.allServers) {
			addHeader(verticalIndex, server, tabList, entries)
			verticalIndex++
			verticalIndex = addPlayers(verticalIndex, server, tabList, entries)
		}

		display(tabList, entries)
	}

	/** Add in the header for a server */
	private fun addHeader(verticalIndex: Int, server: RegisteredServer, tabList: TabList, entries: Array<Array<TabListEntry?>>) {
		val row = entries[verticalIndex]
		row[0] = buildFakePlayer(tabList, text(server.serverInfo.name.replaceFirstChar { it.uppercase() }, RED))
		row[1] = buildFakePlayer(tabList)
		row[2] = buildFakePlayer(tabList, bracketed(ofChildren(text(server.playersConnected.size, HEColorScheme.HE_LIGHT_GRAY), text(" online", GRAY))))
	}

	/**
	 * Add in the players for a server.
	 *
	 * Returns the next empty index
	 **/
	private fun addPlayers(startIndex: Int, server: RegisteredServer, tabList: TabList, entries: Array<Array<TabListEntry?>>): Int {
		var verticalIndex = startIndex
		var horizontalIndex = 0

		for (player in server.playersConnected) {
			if (horizontalIndex >= PLUGIN.tabListConfiguration.columns) {
				horizontalIndex = 0
				verticalIndex++
			}

			entries[verticalIndex][horizontalIndex] = TabListEntry.builder()
				.tabList(tabList)
				.listed(true)
				.latency(player.ping.toInt())
				.displayName(text(player.username)) //TODO prefixes, suffixes
				.gameMode(1)
				.profile(player.gameProfile.withId(UUID.randomUUID()))
				.build()

			horizontalIndex++
		}

		return verticalIndex + 1
	}

	private fun display(tabList: TabList, entries: Array<Array<TabListEntry?>>) {
		tabList.clearAll()
		tabList.entries.forEach { it.setListed(false) }

		for (columnIndex in 0..< PLUGIN.tabListConfiguration.columns) {
			for (rowIndex in entries.indices) {
				val entry = entries[rowIndex][columnIndex]

				if (entry == null) {
					tabList.addEntry(buildFakePlayer(tabList))
					continue
				}

				tabList.addEntry(entry)
			}
		}
	}

	private fun getTotalHeight(servers: Map<RegisteredServer, Int>): Int {
		val baseHeight = PLUGIN.tabListConfiguration.baseHeight

		var total = 0
		for ((_, size) in servers) {
			total += size + 1
		}

		return maxOf(total, baseHeight)
	}

	private fun getSectionHeight(server: RegisteredServer): Int {
		val playerCount = server.playersConnected.size
		val columns = PLUGIN.tabListConfiguration.columns

		return ceil(playerCount.toDouble() / columns.toDouble()).toInt()
	}

	private fun buildFakePlayer(tabList: TabList, displayName: Component = empty()): TabListEntry {
		val icon = PLUGIN.tabListConfiguration.usedIcon.get()

		val profiler = GameProfile(UUID.randomUUID(), "filler", listOf(
			GameProfile.Property(
				"textures",
				icon.texture,
				icon.signature
			)
		))

		return TabListEntry.builder()
			.tabList(tabList)
			.listed(true)
			.latency(-1)
			.gameMode(0)
			.profile(profiler)
			.displayName(displayName)
			.build()
	}
}
