package net.horizonsend.ion.server.features.misc

import club.minnced.discord.webhook.WebhookClient
import club.minnced.discord.webhook.WebhookClientBuilder
import club.minnced.discord.webhook.send.WebhookMessageBuilder
import net.horizonsend.ion.server.LegacySettings
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.starship.event.StarshipPilotedEvent
import net.horizonsend.ion.server.features.starship.event.StarshipUnpilotEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.inventory.InventoryCreativeEvent
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerGameModeChangeEvent

object DutyModeMonitor : IonServerComponent() {
	private fun isInDutyMode(player: Player) = player.hasPermission("group.dutymode")

	@EventHandler
	fun onRunCommand(event: PlayerCommandPreprocessEvent) {
		val player = event.player

		if (!isInDutyMode(player) && !isDutyModeCommand(event)) {
			return
		}

		record(player, "**Command**: ${event.message}")
	}

	private fun isDutyModeCommand(event: PlayerCommandPreprocessEvent) = event.message
		.removePrefix("/")
		.lowercase()
		.startsWith("dutymode")

	@EventHandler
	fun onChangeGameMode(event: PlayerGameModeChangeEvent) {
		val player = event.player

		if (!isInDutyMode(player)) {
			return
		}

		record(player, "**Game Mode**: ${event.player.gameMode} -> ${event.newGameMode}")
	}

	@EventHandler
	fun onTeleport(event: PlayerChangedWorldEvent) {
		val player = event.player

		if (!isInDutyMode(player)) {
			return
		}

		record(player, "**World Change**: ${event.from} -> ${event.player.world}")
	}

	@EventHandler
	fun onCreativeItem(event: InventoryCreativeEvent) {
		val player = event.whoClicked as? Player ?: return

		if (!isInDutyMode(player)) {
			return
		}

		record(player, "**Creative Inventory**: inventoryType=${event.inventory.type}, " +
			"slotType=${event.slotType}, " +
			"slot=${event.slot}, " +
			"newItem/cursor=${event.cursor}")
	}

	@EventHandler
	fun onStarshipPilot(event: StarshipPilotedEvent) {
		val player = event.player

		if (!isInDutyMode(player)) {
			return
		}

		val starship = event.starship
		record(player, "**Starship Pilot**: ${starship.type} (${starship.initialBlockCount} blocks)")
	}

	@EventHandler
	fun onStarshipUnpilot(event: StarshipUnpilotEvent) {
		val player = event.player

		if (!isInDutyMode(player)) {
			return
		}

		val starship = event.starship
		record(player, "**Starship Unpilot**: ${starship.type} (${starship.initialBlockCount} blocks)")
	}

	private fun record(player: Player, content: String) {
		val url = LegacySettings.dutyModeMonitorWebhook ?: return

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
		client.send(WebhookMessageBuilder()
			.setUsername("$name ($uuid)")
			.setAvatarUrl("https://crafatar.com/renders/head/$uuid")
			.setContent(content)
			.build())
	}
}
