package net.horizonsend.ion.server.features.misc

import club.minnced.discord.webhook.WebhookClient
import club.minnced.discord.webhook.WebhookClientBuilder
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.ServerConfiguration
import net.horizonsend.ion.server.features.bounties.Bounties.hasActive
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.event.StarshipPilotedEvent
import net.horizonsend.ion.server.features.starship.event.StarshipUnpilotEvent
import net.horizonsend.ion.server.miscellaneous.utils.listen
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import org.bukkit.event.entity.PlayerDeathEvent

object EventLogger : IonServerComponent() {
	override fun onEnable() {
		val url = ServerConfiguration.eventLoggerWebhook ?: return
		val builder = WebhookClientBuilder(url)

		val client: WebhookClient =  try {
			builder.setThreadFactory { job ->
				val thread = Thread(job)
				thread.name = "Hello"
				thread.isDaemon = true
				thread
			}

			builder.setWait(true)

			builder.build()
		} catch (e: Exception) {
			return
		}

		listen<PlayerDeathEvent> { event ->
			val player = event.player

			val killer = player.killer

			val bounty = killer?.let { hasActive(player.slPlayerId, killer.slPlayerId) } ?: false

			DutyModeMonitor.record(
				client,
				player,
				"**died**: killer: ${event.player.killer}, nearby players: ${event.player.location.getNearbyPlayers(50.0).joinToString { it.name }}, bounty: $bounty"
			)
		}

		listen<StarshipPilotedEvent> { event ->
			val player = event.player

			val starship = event.starship
			DutyModeMonitor.record(client, player, "**piloted starship**: ${starship.type} (${starship.initialBlockCount} blocks)")
		}

		listen<StarshipUnpilotEvent> { event ->
			val controller = event.controller

			if (controller !is PlayerController) return@listen

			val starship = event.starship
			DutyModeMonitor.record(client, controller.player, "**unpiloted starship**: ${starship.type} (${starship.initialBlockCount} blocks)")
		}
	}
}
