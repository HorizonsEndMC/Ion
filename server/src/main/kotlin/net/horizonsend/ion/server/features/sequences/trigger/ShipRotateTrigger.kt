package net.horizonsend.ion.server.features.sequences.trigger

import net.horizonsend.ion.server.features.sequences.trigger.ShipRotateTrigger.ShipRotationTriggerSettings
import net.horizonsend.ion.server.features.starship.event.movement.StarshipRotateEvent
import net.horizonsend.ion.server.miscellaneous.utils.listen
import org.bukkit.entity.Player

object ShipRotateTrigger : SequenceTriggerType<ShipRotationTriggerSettings>() {
	override fun setupChecks() {
		listen<StarshipRotateEvent> {
			val player = it.ship.playerPilot ?: return@listen
			checkAllSequences(player)
		}
	}

	class ShipRotationTriggerSettings(
	) : TriggerSettings() {
		override fun shouldProceed(player: Player, context: TriggerContext): Boolean {
			return context.callingTrigger == ShipRotateTrigger
		}
	}
}
