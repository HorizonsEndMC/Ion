package net.horizonsend.ion.server.features.sequences.trigger

import net.horizonsend.ion.server.features.sequences.trigger.PlayerMovementTrigger.PlayerLocationPredicate
import net.horizonsend.ion.server.features.sequences.trigger.ShipManualFlightTrigger.ShiftFlightTriggerSettings
import net.horizonsend.ion.server.features.starship.event.movement.StarshipTranslateEvent
import net.horizonsend.ion.server.features.starship.movement.TranslateMovement
import net.horizonsend.ion.server.miscellaneous.utils.listen
import org.bukkit.entity.Player

object ShipManualFlightTrigger : SequenceTriggerType<ShiftFlightTriggerSettings>() {
	override fun setupChecks() {
		listen<StarshipTranslateEvent> {
			val player = it.ship.playerPilot ?: return@listen
			if (it.movement.source != TranslateMovement.MovementSource.MANUAL) return@listen
			checkAllSequences(player, it)
		}
	}

	class ShiftFlightTriggerSettings(val predicates: List<PlayerLocationPredicate> = listOf()) : TriggerSettings() {
		override fun shouldProceed(player: Player, context: TriggerContext): Boolean {
			return predicates.all { predicate -> predicate.check(player, context) }
		}
	}
}
