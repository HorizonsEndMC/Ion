package net.horizonsend.ion.server.features.sequences.effect

import net.horizonsend.ion.server.features.sequences.SequenceManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.entity.Player

abstract class SequencePhaseEffect(val playPhases: List<EffectTiming>) {
	abstract fun playEffect(player: Player)

	class EndSequence(playPhases: List<EffectTiming>) : SequencePhaseEffect(playPhases) {
		override fun playEffect(player: Player) { SequenceManager.endPhase(player) }
	}

	class SendMessage(val message: Component, playPhases: List<EffectTiming>) : SequencePhaseEffect(playPhases) {
		override fun playEffect(player: Player) { player.sendMessage(message) }
	}

	class SendTitle(val title: Title, playPhases: List<EffectTiming>) : SequencePhaseEffect(playPhases) {
		override fun playEffect(player: Player) { player.showTitle(title) }
	}
}
