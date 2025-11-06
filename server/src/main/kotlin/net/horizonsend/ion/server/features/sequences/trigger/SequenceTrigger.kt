package net.horizonsend.ion.server.features.sequences.trigger

import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.features.sequences.SequenceManager
import net.horizonsend.ion.server.features.sequences.phases.SequencePhase
import org.bukkit.entity.Player
import org.bukkit.event.Event

class SequenceTrigger<T : SequenceTriggerType.TriggerSettings>(val type: SequenceTriggerType<T>, private val settings: T, private var triggerResult: (Player, TriggerContext) -> Unit) {
	fun trigger(player: Player, context: TriggerContext) {
		triggerResult.invoke(player, context)
	}

	fun shouldProceed(player: Player, context: TriggerContext): Boolean {
		return settings.shouldProceed(player, context)
	}

	companion object {
		fun startPhase(phaseKey: IonRegistryKey<SequencePhase, out SequencePhase>): (Player, TriggerContext) -> Unit = { player, _ ->
			val phase = phaseKey.getValue()
			SequenceManager.startPhase(player, phase.sequenceKey, phase.phaseKey)
		}

		fun <T : Event> handleEvent(handle: (Player, TriggerContext, T) -> Unit): (Player, TriggerContext) -> Unit = handler@{ player, context ->
			@Suppress("UNCHECKED_CAST") val event = context.event as? T ?: return@handler
			handle.invoke(player, context, event)
		}
	}
}
