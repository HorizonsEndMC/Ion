package net.horizonsend.ion.server.features.sequences.trigger

import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.features.sequences.Sequence
import net.horizonsend.ion.server.features.sequences.SequenceContext
import net.horizonsend.ion.server.features.sequences.SequenceManager.getCurrentSequences
import net.horizonsend.ion.server.features.sequences.trigger.ShipEnterHyperspaceJumpTrigger.ShipEnterHyperspaceJumpTriggerSettings
import net.horizonsend.ion.server.features.starship.event.StarshipEnterHyperspaceEvent
import net.horizonsend.ion.server.miscellaneous.utils.listen
import org.bukkit.entity.Player

object ShipEnterHyperspaceJumpTrigger : SequenceTriggerType<ShipEnterHyperspaceJumpTriggerSettings>() {
	override fun setupChecks() {
		listen<StarshipEnterHyperspaceEvent> {
			val player = it.starship.playerPilot ?: return@listen
			for (sequenceKey in getCurrentSequences(player)) { checkPhaseTriggers(player, sequenceKey) }
		}
	}

	class ShipEnterHyperspaceJumpTriggerSettings() : TriggerSettings() {
		override fun shouldProceed(player: Player, sequenceKey: IonRegistryKey<Sequence, out Sequence>, callingTrigger: SequenceTriggerType<*>, context: SequenceContext): Boolean {
			return callingTrigger == ShipEnterHyperspaceJumpTrigger
		}
	}
}
