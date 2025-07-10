package net.horizonsend.ion.server.features.sequences

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.sequences.SequencePhaseKeys.SequencePhaseKey
import net.horizonsend.ion.server.features.sequences.SequencePhaseKeys.TUTORIAL_END
import net.horizonsend.ion.server.features.sequences.SequencePhaseKeys.TUTORIAL_START
import net.horizonsend.ion.server.features.sequences.SequencePhaseKeys.TUTORIAL_TWO
import net.horizonsend.ion.server.features.sequences.effect.EffectTiming
import net.horizonsend.ion.server.features.sequences.effect.SequencePhaseEffect
import net.horizonsend.ion.server.features.sequences.trigger.SequenceTrigger
import net.horizonsend.ion.server.features.sequences.trigger.SequenceTriggerType.PlayerInteractTrigger.InteractTriggerSettings
import net.horizonsend.ion.server.features.sequences.trigger.SequenceTriggerType.PlayerMovementTrigger.MovementTriggerSettings
import net.horizonsend.ion.server.features.sequences.trigger.SequenceTriggerType.PlayerMovementTrigger.PlayerLocationPredicate.Companion.inBoundingBox
import net.horizonsend.ion.server.features.sequences.trigger.SequenceTriggerTypes
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
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

	fun getPhaseByKey(key: SequencePhaseKeys.SequencePhaseKey): SequencePhase {
		return phasesByKey[key] ?: throw IllegalStateException("Unregistered phase key ${key.key}")
	}

	private val phasesByKey = mutableMapOf<SequencePhaseKey, SequencePhase>()

	private fun bootstrapPhase(phase: SequencePhase): SequencePhaseKey {
		phasesByKey[phase.key] = phase
		return phase.key
	}

	val TUTORIAL = registerStartPhase(SequencePhase(
		key = TUTORIAL_START,
		trigger = SequenceTrigger(SequenceTriggerTypes.PLAYER_INTERACT, InteractTriggerSettings()),
		effects = mutableListOf(
			SequencePhaseEffect.SendMessage(Component.text("phase 1 start"), listOf(EffectTiming.START)),
			SequencePhaseEffect.SendMessage(Component.text("phase 1 ticked"), listOf(EffectTiming.TICKED)),
			SequencePhaseEffect.SendMessage(Component.text("phase 1 end"), listOf(EffectTiming.END))
		),
		children = listOf(
			bootstrapPhase(SequencePhase(
				key = TUTORIAL_TWO,
				trigger = SequenceTrigger(SequenceTriggerTypes.PLAYER_MOVEMENT, MovementTriggerSettings(listOf(
					inBoundingBox(Vec3i(193, 359, -121), Vec3i(200, 365, -111), )
				))),
				effects = mutableListOf(
					SequencePhaseEffect.SendMessage(Component.text("phase 2 start"), listOf(EffectTiming.START)),
					SequencePhaseEffect.SendMessage(Component.text("phase 2 ticked"), listOf(EffectTiming.TICKED)),
					SequencePhaseEffect.SendMessage(Component.text("phase 2 end"), listOf(EffectTiming.END))
				),
				children = listOf(bootstrapPhase(SequencePhase.endSequence(TUTORIAL_END, SequenceTrigger(SequenceTriggerTypes.PLAYER_INTERACT, InteractTriggerSettings()))))
			))
		)
	))
}
