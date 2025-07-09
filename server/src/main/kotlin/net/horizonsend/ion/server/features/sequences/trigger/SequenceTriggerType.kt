package net.horizonsend.ion.server.features.sequences.trigger

import net.horizonsend.ion.server.features.sequences.SequenceManager
import net.horizonsend.ion.server.features.sequences.trigger.SequenceTriggerType.PlayerInteractTrigger.InteractTriggerSettings
import net.horizonsend.ion.server.features.sequences.trigger.SequenceTriggerType.PlayerMovementTrigger.MovementTriggerSettings
import net.horizonsend.ion.server.miscellaneous.utils.listen
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent

abstract class SequenceTriggerType<T : SequenceTriggerType.TriggerSettings> {
	open fun setup() {}

	protected fun triggerPhases(player: Player) {
		val currentPhase = SequenceManager.getCurrentPhase(player) ?: return

		for (trigger in currentPhase.danglingTriggers) {
			if (trigger.type != this) continue

			@Suppress("UNCHECKED_CAST")
			trigger as SequenceTrigger<T>

			if (!trigger.settings.shouldProceed(player)) continue

			trigger.trigger(player)
			break
		}
	}

	abstract class TriggerSettings() {
		abstract fun shouldProceed(player: Player): Boolean
	}

	object PlayerMovementTrigger : SequenceTriggerType<MovementTriggerSettings>() {
		override fun setup() {
			listen<PlayerMoveEvent> { triggerPhases(it.player) }
		}

		class MovementTriggerSettings() : TriggerSettings() {
			override fun shouldProceed(player: Player): Boolean {
				return true
			}
		}
	}

	object PlayerInteractTrigger : SequenceTriggerType<InteractTriggerSettings>() {
		override fun setup() {
			listen<PlayerInteractEvent> { triggerPhases(it.player) }
		}

		class InteractTriggerSettings() : TriggerSettings() {
			override fun shouldProceed(player: Player): Boolean {
				return true
			}
		}
	}
}
