package net.horizonsend.ion.server.features.sequences.trigger

import net.horizonsend.ion.server.features.sequences.trigger.StarshipMovementTrigger.StarshipMovementTriggerSettings
import net.horizonsend.ion.server.features.starship.event.movement.StarshipMoveEvent
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import org.bukkit.entity.Player

object StarshipMovementTrigger : SequenceTriggerType<StarshipMovementTriggerSettings>() {

    class StarshipMovementTriggerSettings(val movementPredicate: (Player, StarshipMovement) -> Boolean = { _, _ -> true }) : TriggerSettings() {
        override fun shouldProceed(player: Player, context: TriggerContext): Boolean {
            val event = context.event
            if (event is StarshipMoveEvent) {
                return movementPredicate.invoke(player, event.movement)
            }

            return context.callingTrigger == StarshipMovementTrigger
        }
    }
}