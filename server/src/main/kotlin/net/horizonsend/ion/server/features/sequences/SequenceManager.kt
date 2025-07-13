package net.horizonsend.ion.server.features.sequences

import com.google.common.collect.HashBasedTable
import net.horizonsend.ion.common.utils.getOrPut
import net.horizonsend.ion.common.utils.removeRow
import net.horizonsend.ion.common.utils.set
import net.horizonsend.ion.server.core.IonServerComponent
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.features.sequences.phases.SequencePhase
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys
import net.horizonsend.ion.server.features.sequences.trigger.SequenceTriggerTypes
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.SEQUENCES
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import org.bukkit.Bukkit.getPlayer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.UUID

object SequenceManager : IonServerComponent() {
	private val phaseMap = HashBasedTable.create<UUID, IonRegistryKey<Sequence, out Sequence>, IonRegistryKey<SequencePhase, out SequencePhase>>()
	private val sequenceData = HashBasedTable.create<UUID, IonRegistryKey<Sequence, out Sequence>, SequenceDataStore>()

	override fun onEnable() {
		SequenceTriggerTypes.runSetup()

		Tasks.asyncRepeat(1L, 1L) {
			tickPhases()
		}
	}

	fun getSequenceData(player: Player, sequenceKey: IonRegistryKey<Sequence, out Sequence>): SequenceDataStore {
		return sequenceData.getOrPut(player.uniqueId, sequenceKey) { SequenceDataStore() }
	}

	fun clearSequenceData(player: Player) {
		sequenceData.removeRow(player.uniqueId)
	}

	@EventHandler
	fun onPlayerLeave(event: PlayerQuitEvent) {
		val uuid = event.player.uniqueId

		saveSequenceData(event.player)

		phaseMap.removeRow(uuid)
		sequenceData.removeRow(uuid)
	}

	fun saveSequenceData(player: Player) {
		val sequenceKeys = mutableListOf<IonRegistryKey<Sequence, out Sequence>>()

		phaseMap.rowMap()[player.uniqueId]?.let { map ->
			for ((sequenceKey, phase) in map) {
				phase.getValue().endPrematurely(player)

				player.persistentDataContainer.set(
					sequenceKey.ionNapespacedKey,
					QuestData,
					QuestData(phase.key, getSequenceData(player, sequenceKey).metaDataMirror)
				)

				sequenceKeys.add(sequenceKey)
			}
		}

		player.persistentDataContainer.set(SEQUENCES, SequenceKeys.listSerializer, sequenceKeys)
	}

	@EventHandler
	fun onPlayerJoin(event: PlayerJoinEvent) {
		val activeSequences = event.player.persistentDataContainer.get(SEQUENCES, SequenceKeys.listSerializer) ?: return

		for (sequenceKey in activeSequences) {
			val namespacedKey = sequenceKey.ionNapespacedKey

			val questData = event.player.persistentDataContainer.get(
				namespacedKey,
				QuestData
			) ?: continue


			val b = SequencePhaseKeys[questData.currentPhase]!!
			phaseMap[event.player.uniqueId, sequenceKey] = b
			sequenceData[event.player.uniqueId, sequenceKey] = questData.unpackDataStore()
		}
	}

	fun getCurrentPhase(player: Player, sequenceKey: IonRegistryKey<Sequence, out Sequence>): IonRegistryKey<SequencePhase, out SequencePhase>? {
		return phaseMap[player.uniqueId, sequenceKey]
	}

	fun getCurrentSequences(player: Player): Set<IonRegistryKey<Sequence, out Sequence>> {
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

	fun startPhase(player: Player, sequenceKey: IonRegistryKey<Sequence, out Sequence>, phase: IonRegistryKey<SequencePhase, out SequencePhase>?) {
		if (phase == null) {
			endPhase(player, sequenceKey)
			saveSequenceData(player)

			return
		}

		setPhase(player, sequenceKey, phase)
		phase.getValue().start(player)
		saveSequenceData(player)
	}

	fun endPhase(player: Player, sequenceKey: IonRegistryKey<Sequence, out Sequence>) {
		val existingPhase = phaseMap.remove(player.uniqueId, sequenceKey)
		existingPhase?.getValue()?.end(player)
		saveSequenceData(player)
	}

	private fun setPhase(player: Player, sequenceKey: IonRegistryKey<Sequence, out Sequence>, phase: IonRegistryKey<SequencePhase, out SequencePhase>) {
		endPhase(player, sequenceKey)
		phaseMap[player.uniqueId, sequenceKey] = phase
	}
}
