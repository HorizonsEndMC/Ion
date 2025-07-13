package net.horizonsend.ion.server.features.sequences

import net.horizonsend.ion.server.core.IonServerComponent
import net.horizonsend.ion.server.features.sequences.phases.SequencePhase
import net.horizonsend.ion.server.features.sequences.phases.SequencePhases
import net.horizonsend.ion.server.features.sequences.trigger.SequenceTriggerTypes
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import org.bukkit.Bukkit.getPlayer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerQuitEvent
import java.util.UUID

object SequenceManager : IonServerComponent() {
	private val phaseMap = mutableMapOf<UUID, SequencePhase>()
	private val sequenceData = mutableMapOf<UUID, SequenceDataStore>()

	override fun onEnable() {
		SequenceTriggerTypes.runSetup()
		SequencePhases.registerPhases()

		Tasks.asyncRepeat(1L, 1L) {
			tickPhases()
		}
	}

	fun getSequenceData(player: Player): SequenceDataStore {
		return sequenceData.getOrPut(player.uniqueId) { SequenceDataStore() }
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
}
