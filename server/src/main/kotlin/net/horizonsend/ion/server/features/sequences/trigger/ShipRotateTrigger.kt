package net.horizonsend.ion.server.features.sequences.trigger

import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.features.sequences.Sequence
import net.horizonsend.ion.server.features.sequences.SequenceContext
import net.horizonsend.ion.server.features.sequences.SequenceManager.getCurrentSequences
import net.horizonsend.ion.server.features.sequences.trigger.ShipRotateTrigger.ShipRotationTriggerSettings
import net.horizonsend.ion.server.features.starship.event.movement.StarshipRotateEvent
import net.horizonsend.ion.server.miscellaneous.utils.listen
import org.bukkit.entity.Player

object ShipRotateTrigger : SequenceTriggerType<ShipRotationTriggerSettings>() {
	override fun setupChecks() {
		listen<StarshipRotateEvent> {
			val player = it.ship.playerPilot ?: return@listen
			for (sequenceKey in getCurrentSequences(player)) { checkPhaseTriggers(player, sequenceKey) }
		}
	}

	class ShipRotationTriggerSettings(
	) : TriggerSettings() {
		override fun shouldProceed(player: Player, sequenceKey: IonRegistryKey<Sequence, out Sequence>, callingTrigger: SequenceTriggerType<*>, context: SequenceContext): Boolean {
			return callingTrigger == ShipRotateTrigger
		}
	}
}
