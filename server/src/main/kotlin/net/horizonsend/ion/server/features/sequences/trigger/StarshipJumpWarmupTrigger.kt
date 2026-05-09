package net.horizonsend.ion.server.features.sequences.trigger

import net.horizonsend.ion.server.features.sequences.trigger.StarshipJumpWarmupTrigger.StarshipJumpWarmupTriggerSettings
import net.horizonsend.ion.server.features.starship.event.StarshipJumpWarmupEvent
import net.horizonsend.ion.server.miscellaneous.utils.listen
import org.bukkit.entity.Player

object StarshipJumpWarmupTrigger : SequenceTriggerType<StarshipJumpWarmupTriggerSettings>() {
    override fun setupChecks() {
        listen<StarshipJumpWarmupEvent> {
            val player = it.starship.playerPilot ?: return@listen
            checkAllSequences(player, it)
        }
    }

    class StarshipJumpWarmupTriggerSettings : TriggerSettings() {
        override fun shouldProceed(player: Player, context: TriggerContext): Boolean {
            return context.callingTrigger == StarshipJumpWarmupTrigger
        }
    }
}