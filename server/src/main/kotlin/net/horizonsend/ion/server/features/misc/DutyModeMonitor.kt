package net.horizonsend.ion.server.features.misc

import club.minnced.discord.webhook.WebhookClient
import club.minnced.discord.webhook.WebhookClientBuilder
import club.minnced.discord.webhook.send.WebhookMessageBuilder
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.ServerConfiguration
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.event.StarshipPilotedEvent
import net.horizonsend.ion.server.features.starship.event.StarshipUnpilotEvent
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.listen
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerGameModeChangeEvent

object DutyModeMonitor : IonServerComponent() {
	private fun isInDutyMode(player: Player) = player.hasPermission("group.dutymode")

	override fun onEnable() {
		val webhook = ServerConfiguration.dutyModeMonitorWebhook ?: return

		listen<PlayerCommandPreprocessEvent> { event ->
			val player = event.player

			if (!isInDutyMode(player) && !isDutyModeCommand(event)) {
				return@listen
			}

			record(player, "**executed command**: ${event.message}", webhook)
		}

		listen<PlayerGameModeChangeEvent> { event ->
			val player = event.player

			if (!isInDutyMode(player)) {
				return@listen
			}

			record(player, "**changed game mode**: ${event.player.gameMode} -> ${event.newGameMode}", webhook)
		}

		listen<PlayerChangedWorldEvent> { event ->
			val player = event.player

			if (!isInDutyMode(player)) {
				return@listen
			}

			record(player, "**changed world**: ${event.from} -> ${event.player.world}", webhook)
		}

		listen<InventoryClickEvent> { event ->
			val player = event.whoClicked as? Player ?: return@listen

			if (!isInDutyMode(player)) {
				return@listen
			}

			record(
				player,
				"**Added to their creative inventory**: inventoryType=${event.inventory.type}, " +
				"slotType=${event.slotType}, " +
				"slot=${event.slot}, " +
				"newItem/cursor=${event.cursor}",
				webhook
			)
		}

		listen<StarshipPilotedEvent> { event ->
			val player = event.player

			if (!isInDutyMode(player)) {
				return@listen
			}

			val starship = event.starship
			record(player, "*piloted starship**: ${starship.type} (${starship.initialBlockCount} blocks)", webhook)
		}

		listen<StarshipUnpilotEvent> { event ->
			val controller = event.controller

			if (controller !is PlayerController) return@listen

			if (!isInDutyMode(controller.player)) {
				return@listen
			}

			val starship = event.starship
			record(controller.player, "**unpiloted starship**: ${starship.type} (${starship.initialBlockCount} blocks)", webhook)
		}
	}

	private fun isDutyModeCommand(event: PlayerCommandPreprocessEvent) = event.message
		.removePrefix("/")
		.lowercase()
		.startsWith("dutymode")

	fun record(player: Player, content: String, url: String) {
		val builder = WebhookClientBuilder(url)

		builder.setThreadFactory { job ->
			val thread = Thread(job)
			thread.name = "Hello"
			thread.isDaemon = true
			thread
		}

		builder.setWait(true)

		val client: WebhookClient = builder.build()
		val name = player.name
		val uuid = player.uniqueId

		val (x, y, z) = Vec3i(player.location)

		client.send(WebhookMessageBuilder()
			.setUsername("$name ($uuid)")
			.setAvatarUrl("https://crafatar.com/renders/head/$uuid")
			.setContent("${player.name}: $content at $x $y $z at ${player.location.world}")
			.build())
	}
}
