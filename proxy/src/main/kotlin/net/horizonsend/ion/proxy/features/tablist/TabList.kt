package net.horizonsend.ion.proxy.features.tablist

import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.server.RegisteredServer
import com.velocitypowered.api.scheduler.ScheduledTask
import net.horizonsend.ion.common.utils.configuration.redis
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

object TabList : IonProxyComponent() {
	const val COLUMNS = 3

	private lateinit var playerTickTask: ScheduledTask

	override fun onEnable() {
		playerTickTask = PLUGIN.server.scheduler.buildTask(PLUGIN, ::tickPlayers)
			.repeat(Duration.ofSeconds(1))
			.schedule()
	}

	override fun onDisable() {
		playerTickTask.cancel()
	}

	fun updateTabList() {

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
}
