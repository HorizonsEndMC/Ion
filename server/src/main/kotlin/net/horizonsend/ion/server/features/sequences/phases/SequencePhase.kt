package net.horizonsend.ion.server.features.sequences.phases

import net.horizonsend.ion.server.features.sequences.SequenceManager
import net.horizonsend.ion.server.features.sequences.effect.EffectTiming
import net.horizonsend.ion.server.features.sequences.effect.SequencePhaseEffect
import net.horizonsend.ion.server.features.sequences.trigger.SequenceTrigger
import org.bukkit.entity.Player

class SequencePhase(
	val sequenceKey: String,
	val phaseKey: SequencePhaseKeys.SequencePhaseKey,
	val trigger: SequenceTrigger<*>?,

	effects: List<SequencePhaseEffect>,

	val children: List<SequencePhaseKeys.SequencePhaseKey>
) {
	private val startEffects = effects.filter { effect -> effect.playPhases.contains(EffectTiming.START) }
	private val tickedEffects = effects.filter { effect -> effect.playPhases.contains(EffectTiming.TICKED) }
	private val endEffects = effects.filter { effect -> effect.playPhases.contains(EffectTiming.END) }

	init {
	    trigger?.setTriggerResult { player -> SequenceManager.startPhase(player, sequenceKey, phaseKey) }
	}

	val danglingTriggers get() = children.mapNotNull { phase -> phase.getValue().trigger }

	fun start(player: Player) {
		startEffects.forEach { it.playEffect(player, sequenceKey) }
	}

	fun tick(player: Player) {
		tickedEffects.forEach { it.playEffect(player, sequenceKey) }
	}

	fun end(player: Player) {
		endEffects.forEach { it.playEffect(player, sequenceKey) }
		SequenceManager.getSequenceData(player, sequenceKey).set("last_phase", phaseKey)
	}

	fun endPrematurely(player: Player) {

	}

	companion object {
		fun endSequence(sequenceKey: String, key: SequencePhaseKeys.SequencePhaseKey, trigger: SequenceTrigger<*>, vararg effect: SequencePhaseEffect): SequencePhase = SequencePhase(
			sequenceKey = sequenceKey,
			phaseKey = key,
			trigger = trigger,
			effects = listOf(
				SequencePhaseEffect.EndSequence(listOf(EffectTiming.START)),
				SequencePhaseEffect.ClearSequenceData(listOf(EffectTiming.START)),
				*effect
			),
			children = listOf()
		)
	}
}
