package net.horizonsend.ion.server.features.sequences

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.sequences.SequencePhaseKeys.CHERRY_TEST_BRANCH
import net.horizonsend.ion.server.features.sequences.SequencePhaseKeys.SequencePhaseKey
import net.horizonsend.ion.server.features.sequences.SequencePhaseKeys.TUTORIAL_END
import net.horizonsend.ion.server.features.sequences.SequencePhaseKeys.TUTORIAL_START
import net.horizonsend.ion.server.features.sequences.SequencePhaseKeys.TUTORIAL_TWO
import net.horizonsend.ion.server.features.sequences.effect.EffectTiming
import net.horizonsend.ion.server.features.sequences.effect.SequencePhaseEffect
import net.horizonsend.ion.server.features.sequences.trigger.SequenceTrigger
import net.horizonsend.ion.server.features.sequences.trigger.SequenceTriggerType
import net.horizonsend.ion.server.features.sequences.trigger.SequenceTriggerType.PlayerInteractTrigger.InteractTriggerSettings
import net.horizonsend.ion.server.features.sequences.trigger.SequenceTriggerType.PlayerMovementTrigger.MovementTriggerSettings
import net.horizonsend.ion.server.features.sequences.trigger.SequenceTriggerType.PlayerMovementTrigger.PlayerLocationPredicate.Companion.lookingAtBoundingBox
import net.horizonsend.ion.server.features.sequences.trigger.SequenceTriggerTypes
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit.getPlayer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.util.BoundingBox
import java.util.UUID

object SequenceManager : IonServerComponent() {
	private val phaseMap = mutableMapOf<UUID, SequencePhase>()
	private val sequenceData = mutableMapOf<UUID, SequenceDataStore>()

	override fun onEnable() {
		SequenceTriggerTypes.runSetup()

		Tasks.asyncRepeat(1L, 1L) {
			tickPhases()
		}
	}

	fun getSequenceData(player: Player): SequenceDataStore {
		return sequenceData.getOrPut(player.uniqueId) { SequenceDataStore(player.uniqueId) }
	}

	fun clearSequenceData(player: Player) {
		sequenceData.remove(player.uniqueId)
	}

	@EventHandler
	fun onPlayerLeave(event: PlayerQuitEvent) {
		phaseMap.remove(event.player.uniqueId)?.endPrematurely(event.player)
		sequenceData.remove(event.player.uniqueId)
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

	fun getPhaseByKey(key: SequencePhaseKey): SequencePhase {
		return phasesByKey[key] ?: throw IllegalStateException("Unregistered phase key ${key.key}")
	}

	private val phasesByKey = mutableMapOf<SequencePhaseKey, SequencePhase>()

	private fun bootstrapPhase(phase: SequencePhase): SequencePhaseKey {
		phasesByKey[phase.key] = phase
		return phase.key
	}

	val TUTORIAL get() = bootstrapPhase(SequencePhase(
		key = TUTORIAL_START,
		trigger = SequenceTrigger(SequenceTriggerTypes.PLAYER_INTERACT, InteractTriggerSettings()),
		effects = mutableListOf(
			SequencePhaseEffect.SendMessage(Component.text("Go look at the door to progresss"), listOf(EffectTiming.START)),
		),
		children = listOf(
			bootstrapPhase(SequencePhase(
				key = TUTORIAL_TWO,
				trigger = SequenceTrigger(SequenceTriggerTypes.PLAYER_MOVEMENT, MovementTriggerSettings(listOf(
					lookingAtBoundingBox(BoundingBox.of(Vec3i(193, 359, -121).toVector(), Vec3i(200, 365, -111).plus(Vec3i(1, 1, 1)).toVector()))
				))),
				effects = mutableListOf(
					SequencePhaseEffect.SendMessage(Component.text("Punch to progress"), listOf(EffectTiming.START)),
				),
				children = listOf(bootstrapPhase(
					SequencePhase.endSequence(
						key = TUTORIAL_END,
						trigger = SequenceTrigger(SequenceTriggerTypes.PLAYER_INTERACT, InteractTriggerSettings())
					)))
			)),
			bootstrapPhase(SequencePhase(
				key = CHERRY_TEST_BRANCH,
				trigger = SequenceTrigger(SequenceTriggerTypes.COMBINED_AND, SequenceTriggerType.CombinedAndTrigger.CombinedAndTriggerSettings(listOf(
					SequenceTrigger(SequenceTriggerTypes.PLAYER_MOVEMENT, MovementTriggerSettings(listOf(
						lookingAtBoundingBox(BoundingBox.of(Vec3i(203, 360, -126).toVector(), Vec3i(203, 360, -124).plus(Vec3i(1, 1, 1)).toVector()))
					))),
					SequenceTrigger(SequenceTriggerTypes.DATA_PREDICATE, SequenceTriggerType.DataPredicate.DataPredicateSettings<Boolean>("seen_cherry_wood") { it != true })
				))),
				effects = mutableListOf(
					SequencePhaseEffect.SendMessage(Component.text("That is some cherry wood"), listOf(EffectTiming.START)),
					SequencePhaseEffect.SendMessage(Component.text("Back to our regularly scheduled programming"), listOf(EffectTiming.END)),
					SequencePhaseEffect.SetSequenceData("seen_cherry_wood", true, listOf(EffectTiming.END)),
					SequencePhaseEffect.GoToPhase(TUTORIAL_START, listOf(EffectTiming.START))
				),
				children = listOf()
			))
		)
	))
}
