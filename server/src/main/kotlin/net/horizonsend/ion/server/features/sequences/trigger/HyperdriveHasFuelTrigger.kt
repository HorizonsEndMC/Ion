package net.horizonsend.ion.server.features.sequences.trigger

import com.destroystokyo.paper.event.server.ServerTickEndEvent
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.miscellaneous.utils.listen

object HyperdriveHasFuelTrigger : SequenceTriggerType<SimpleContextTriggerPredicate>() {
	override fun setupChecks() {
		listen<ServerTickEndEvent> {
			for (starship in ActiveStarships.allControlledStarships()) {
				val player = starship.playerPilot ?: continue
				if (starship.hyperdrives.none { it.hasFuel() }) continue
				checkAllSequences(player, null)
			}
		}
	}
}
