package net.horizonsend.ion.server.features.sequences.trigger

import net.horizonsend.ion.server.features.sequences.trigger.ShipPreExitHyperspaceJumpTrigger.ShipPreExitHyperspaceJumpTriggerSettings
import net.horizonsend.ion.server.features.starship.event.StarshipPreExitHyperspaceEvent
import net.horizonsend.ion.server.miscellaneous.utils.listen
import org.bukkit.entity.Player

object ShipPreExitHyperspaceJumpTrigger : SequenceTriggerType<ShipPreExitHyperspaceJumpTriggerSettings>() {
	override fun setupChecks() {
		listen<StarshipPreExitHyperspaceEvent> {
			val player = it.starship.playerPilot ?: return@listen
			checkAllSequences(player, it)
		}
	}

	class ShipPreExitHyperspaceJumpTriggerSettings() : TriggerSettings() {
		override fun shouldProceed(player: Player, context: TriggerContext): Boolean {
			return context.callingTrigger == ShipPreExitHyperspaceJumpTrigger
		}
	}
}
