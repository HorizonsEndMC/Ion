package net.horizonsend.ion.server.features.sequences

import net.horizonsend.ion.server.features.sequences.effect.EffectTiming
import net.horizonsend.ion.server.features.sequences.effect.SequencePhaseEffect
import net.horizonsend.ion.server.features.sequences.trigger.SequenceTrigger
import org.bukkit.entity.Player

class SequencePhase(
    val name: String,
    val trigger: SequenceTrigger<*>,

	effects: List<SequencePhaseEffect>,

    val children: List<SequencePhase>
) {
	private val startEffects = effects.filter { effect -> effect.playPhases.contains(EffectTiming.START) }
	private val tickedEffects = effects.filter { effect -> effect.playPhases.contains(EffectTiming.TICKED) }
	private val endEffects = effects.filter { effect -> effect.playPhases.contains(EffectTiming.END) }

	init {
	    trigger.setTriggerResult { player -> SequenceManager.startPhase(player, this@SequencePhase) }
	}

	val danglingTriggers get() = children.map { phase -> phase.trigger }

	fun start(player: Player) {
		startEffects.forEach { it.playEffect(player) }
	}

	fun tick(player: Player) {
		tickedEffects.forEach { it.playEffect(player) }
	}

	fun end(player: Player) {
		endEffects.forEach { it.playEffect(player) }
	}

	fun endPrematurely(player: Player) {

	}

	companion object {
		fun endSequence(trigger: SequenceTrigger<*>): SequencePhase = SequencePhase("END", trigger, listOf(SequencePhaseEffect.EndSequence(listOf(EffectTiming.START))), listOf())
	}
}
