package net.horizonsend.ion.server.features.sequences.trigger

import net.horizonsend.ion.server.features.starship.event.StarshipJumpWarmupEvent
import net.horizonsend.ion.server.miscellaneous.utils.listen

object StarshipJumpWarmupTrigger : SequenceTriggerType<SimpleContextTriggerPredicate>() {
    override fun setupChecks() {
        listen<StarshipJumpWarmupEvent> {
            val player = it.starship.playerPilot ?: return@listen
            checkAllSequences(player, it)
        }
    }
}