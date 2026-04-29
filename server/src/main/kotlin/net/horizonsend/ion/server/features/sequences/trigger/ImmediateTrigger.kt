package net.horizonsend.ion.server.features.sequences.trigger

import org.bukkit.entity.Player

object ImmediateTrigger : SequenceTriggerType<ImmediateTrigger.ImmediateTriggerSettings>() {
    override fun setupChecks() {}

    class ImmediateTriggerSettings() : TriggerSettings() {
        override fun shouldProceed(player: Player, context: TriggerContext): Boolean = true
    }
}