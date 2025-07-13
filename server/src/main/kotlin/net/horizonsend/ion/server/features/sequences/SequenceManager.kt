package net.horizonsend.ion.server.features.sequences

import com.google.common.collect.HashBasedTable
import net.horizonsend.ion.common.utils.getOrPut
import net.horizonsend.ion.common.utils.removeRow
import net.horizonsend.ion.common.utils.set
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.core.IonServerComponent
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.SequencePhaseKey
import net.horizonsend.ion.server.features.sequences.phases.SequencePhases
import net.horizonsend.ion.server.features.sequences.trigger.SequenceTriggerTypes
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.SEQUENCES
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import org.bukkit.Bukkit.getPlayer
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.persistence.ListPersistentDataType
import java.util.UUID

object SequenceManager : IonServerComponent() {
	private val phaseMap = HashBasedTable.create<UUID, String, SequencePhaseKey>()
	private val sequenceData = HashBasedTable.create<UUID, String, SequenceDataStore>()

	override fun onEnable() {
		SequenceTriggerTypes.runSetup()
		SequencePhases.registerPhases()

		Tasks.asyncRepeat(1L, 1L) {
			tickPhases()
		}
	}

	fun getSequenceData(player: Player, sequenceKey: String): SequenceDataStore {
		return sequenceData.getOrPut(player.uniqueId, sequenceKey) { SequenceDataStore() }
	}

	fun clearSequenceData(player: Player) {
		sequenceData.removeRow(player.uniqueId)
	}

	@EventHandler
	fun onPlayerLeave(event: PlayerQuitEvent) {
		val uuid = event.player.uniqueId

		val sequenceKeys = mutableListOf<String>()

		phaseMap.rowMap()[uuid]?.let { map ->
			for ((sequenceKey, phase) in map) {
				phase.getValue().endPrematurely(event.player)

				event.player.persistentDataContainer.set(
					NamespacedKey(IonServer, sequenceKey),
					QuestData,
					QuestData(phase.key, getSequenceData(event.player, sequenceKey).metaDataMirror)
				)

				sequenceKeys.add(sequenceKey)
			}
		}

		event.player.persistentDataContainer.set(SEQUENCES, ListPersistentDataType.LIST.strings(), sequenceKeys)

		phaseMap.removeRow(uuid)
		sequenceData.removeRow(uuid)
	}

	@EventHandler
	fun onPlayerJoin(event: PlayerJoinEvent) {
		val sequences = event.player.persistentDataContainer.get(SEQUENCES, ListPersistentDataType.LIST.strings()) ?: return

		for (sequenceKey in sequences) {
			val namespacedKey = NamespacedKey(IonServer, sequenceKey)

			val questData = event.player.persistentDataContainer.get(
				namespacedKey,
				QuestData
			) ?: continue

			phaseMap[event.player.uniqueId, sequenceKey] = SequencePhaseKeys.byString[questData.currentPhase]!!
			sequenceData[event.player.uniqueId, sequenceKey] = questData.unpackDataStore()
		}
	}

	fun getCurrentPhase(player: Player, sequenceKey: String): SequencePhaseKey? {
		return phaseMap[player.uniqueId, sequenceKey]
	}

	fun getCurrentSequences(player: Player): Collection<String> {
		return phaseMap.rowMap()[player.uniqueId]?.keys ?: setOf()
	}

	fun tickPhases() {
		for ((playerId, sequenceData) in phaseMap.rowMap()) {
			for ((_, sequencePhaseKey) in sequenceData) {
				val player = getPlayer(playerId) ?: continue
				sequencePhaseKey.getValue().tick(player)
			}
		}
	}

	fun startPhase(player: Player, sequenceKey: String, phase: SequencePhaseKey?) {
		if (phase == null) {
			endPhase(player, sequenceKey)

			return
		}

		setPhase(player, sequenceKey, phase)
		phase.getValue().start(player)
	}

	fun endPhase(player: Player, sequenceKey: String) {
		val existingPhase = phaseMap.remove(player.uniqueId, sequenceKey)
		existingPhase?.getValue()?.end(player)
	}

	private fun setPhase(player: Player, sequenceKey: String, phase: SequencePhaseKey) {
		endPhase(player, sequenceKey)
		phaseMap[player.uniqueId, sequenceKey] = phase
	}
}
