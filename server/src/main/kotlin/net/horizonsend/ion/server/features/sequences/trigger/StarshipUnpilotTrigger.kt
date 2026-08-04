package net.horizonsend.ion.server.features.sequences.trigger

import net.horizonsend.ion.server.features.starship.event.StarshipUnpilotEvent
import net.horizonsend.ion.server.miscellaneous.utils.listen
import org.bukkit.entity.Player

object StarshipUnpilotTrigger : SequenceTriggerType<StarshipUnpilotTrigger.StarshipUnpilotTriggerSettings>() {
    override fun setupChecks() {
        listen<StarshipUnpilotEvent> {
            val player = it.starship.playerPilot ?: return@listen
            checkAllSequences(player, it)
        }
    }

    class StarshipUnpilotTriggerSettings : TriggerSettings() {
        override fun shouldProceed(player: Player, context: TriggerContext): Boolean {
            return context.callingTrigger == StarshipUnpilotTrigger
        }
    }
}