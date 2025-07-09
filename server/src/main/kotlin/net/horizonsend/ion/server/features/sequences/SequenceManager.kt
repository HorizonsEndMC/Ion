package net.horizonsend.ion.server.features.sequences

import net.horizonsend.ion.server.core.IonServerComponent
import net.horizonsend.ion.server.features.sequences.effect.EffectTiming
import net.horizonsend.ion.server.features.sequences.effect.SequencePhaseEffect
import net.horizonsend.ion.server.features.sequences.trigger.SequenceTrigger
import net.horizonsend.ion.server.features.sequences.trigger.SequenceTriggerType.PlayerInteractTrigger.InteractTriggerSettings
import net.horizonsend.ion.server.features.sequences.trigger.SequenceTriggerType.PlayerMovementTrigger.MovementTriggerSettings
import net.horizonsend.ion.server.features.sequences.trigger.SequenceTriggerTypes
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit.getPlayer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerQuitEvent
import java.util.UUID

object SequenceManager : IonServerComponent() {
	private val phaseMap = mutableMapOf<UUID, SequencePhase>()

	override fun onEnable() {
		SequenceTriggerTypes.runSetup()

		Tasks.asyncRepeat(1L, 1L) {
			tickPhases()
		}
	}

	@EventHandler
	fun onPlayerLeave(event: PlayerQuitEvent) {
		phaseMap.remove(event.player.uniqueId)?.endPrematurely(event.player)
	}

	fun getCurrentPhase(player: Player): SequencePhase? {
		return phaseMap[player.uniqueId]
	}

	fun tickPhases() {
		for ((playerId, phase) in phaseMap) {
			val player = getPlayer(playerId) ?: continue
			phase.tick(player)
		}
	}

	fun startPhase(player: Player, phase: SequencePhase?) {
		if (phase == null) {
			endPhase(player)

			return
		}

		setPhase(player, phase)
		phase.start(player)
	}

	fun endPhase(player: Player) {
		val existingPhase = phaseMap.remove(player.uniqueId)
		existingPhase?.end(player)
	}

	private fun setPhase(player: Player, phase: SequencePhase) {
		endPhase(player)
		phaseMap[player.uniqueId] = phase
	}

	private fun registerStartPhase(phase: SequencePhase): SequencePhase {
		return phase
	}

	val TUTORIAL = registerStartPhase(SequencePhase(
		name = "test",
		trigger = SequenceTrigger(SequenceTriggerTypes.PLAYER_INTERACT, InteractTriggerSettings()),
		effects = mutableListOf(
			SequencePhaseEffect.SendMessage(Component.text("phase 1 start"), listOf(EffectTiming.START)),
			SequencePhaseEffect.SendMessage(Component.text("phase 1 ticked"), listOf(EffectTiming.TICKED)),
			SequencePhaseEffect.SendMessage(Component.text("phase 1 end"), listOf(EffectTiming.END))
		),
		children = listOf(
			SequencePhase(
				name = "test",
				trigger = SequenceTrigger(SequenceTriggerTypes.PLAYER_MOVEMENT, MovementTriggerSettings()),
				effects = mutableListOf(
					SequencePhaseEffect.SendMessage(Component.text("phase 2 start"), listOf(EffectTiming.START)),
					SequencePhaseEffect.SendMessage(Component.text("phase 2 ticked"), listOf(EffectTiming.TICKED)),
					SequencePhaseEffect.SendMessage(Component.text("phase 2 end"), listOf(EffectTiming.END))
				),
				children = listOf(SequencePhase.endSequence(SequenceTrigger(SequenceTriggerTypes.PLAYER_INTERACT, InteractTriggerSettings())))
			)
		)
	))
}
