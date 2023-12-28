package net.horizonsend.ion.server.features.misc

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.ServerConfiguration
import net.horizonsend.ion.server.features.bounties.Bounties.hasActive
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.event.StarshipPilotedEvent
import net.horizonsend.ion.server.features.starship.event.StarshipUnpilotEvent
import net.horizonsend.ion.server.miscellaneous.utils.listen
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent

object EventLogger : IonServerComponent() {
	override fun onEnable() {
		val webhook = ServerConfiguration.eventLoggerWebhook ?: return

		listen<PlayerCommandPreprocessEvent> { event ->
			val player = event.player

			DutyModeMonitor.record(player, "**executed command**: ${event.message}", webhook)
		}

		listen<PlayerDeathEvent> { event ->
			val player = event.player

			val killer = player.killer

			val bounty = killer?.let { hasActive(player.slPlayerId, killer.slPlayerId) } ?: false

			DutyModeMonitor.record(
				player,
				"**died**: killer: ${event.player.killer}, nearby players: ${event.player.location.getNearbyPlayers(50.0).joinToString { it.name }}, bounty: $bounty",
				webhook
			)
		}

		listen<StarshipPilotedEvent> { event ->
			val player = event.player

			val starship = event.starship
			DutyModeMonitor.record(player, "*piloted starship**: ${starship.type} (${starship.initialBlockCount} blocks)", webhook)
		}

		listen<StarshipUnpilotEvent> { event ->
			val controller = event.controller

			if (controller !is PlayerController) return@listen

			val starship = event.starship
			DutyModeMonitor.record(controller.player, "**unpiloted starship**: ${starship.type} (${starship.initialBlockCount} blocks)", webhook)
		}
	}
}
