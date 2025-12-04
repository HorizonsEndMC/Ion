package net.horizonsend.ion.server.features.sequences.trigger

import net.horizonsend.ion.server.features.sequences.trigger.ShipEnterHyperspaceJumpTrigger.ShipEnterHyperspaceJumpTriggerSettings
import net.horizonsend.ion.server.features.starship.event.StarshipEnterHyperspaceEvent
import net.horizonsend.ion.server.miscellaneous.utils.listen
import org.bukkit.entity.Player

object ShipEnterHyperspaceJumpTrigger : SequenceTriggerType<ShipEnterHyperspaceJumpTriggerSettings>() {
	override fun setupChecks() {
		listen<StarshipEnterHyperspaceEvent> {
			val player = it.starship.playerPilot ?: return@listen
			checkAllSequences(player, it)
		}
	}

	class ShipEnterHyperspaceJumpTriggerSettings() : TriggerSettings() {
		override fun shouldProceed(player: Player, context: TriggerContext): Boolean {
			return context.callingTrigger == ShipEnterHyperspaceJumpTrigger
		}
	}
}
