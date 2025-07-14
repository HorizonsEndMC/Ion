package net.horizonsend.ion.server.features.sequences.phases

import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.features.sequences.Sequence
import net.horizonsend.ion.server.features.sequences.SequenceManager
import net.horizonsend.ion.server.features.sequences.effect.EffectTiming
import net.horizonsend.ion.server.features.sequences.effect.SequencePhaseEffect
import net.horizonsend.ion.server.features.sequences.trigger.SequenceTrigger
import org.bukkit.entity.Player

class SequencePhase(
	val phaseKey: IonRegistryKey<SequencePhase, SequencePhase>,
	val sequenceKey: IonRegistryKey<Sequence, Sequence>,

	val triggers: Collection<SequenceTrigger<*>>,

	effects: List<SequencePhaseEffect>
) {
	private val startEffects = effects.filter { effect -> effect.timing == EffectTiming.START }
	private val tickedEffects = effects.filter { effect -> effect.timing == EffectTiming.TICKED }
	private val endEffects = effects.filter { effect -> effect.timing == EffectTiming.END }

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
		SequenceManager.saveSequenceData(player)
	}

	companion object {
		fun endSequence(sequenceKey: IonRegistryKey<Sequence, Sequence>, key: IonRegistryKey<SequencePhase, SequencePhase>, triggers: Collection<SequenceTrigger<*>>, vararg effect: SequencePhaseEffect): SequencePhase = SequencePhase(
			phaseKey = key,
			sequenceKey = sequenceKey,
			triggers = triggers,
			effects = listOf(
				SequencePhaseEffect.EndSequence(EffectTiming.START),
				SequencePhaseEffect.ClearSequenceData(EffectTiming.START),
				*effect
			)
		)
	}
}
