package net.horizonsend.ion.server.features.sequences.trigger

import net.horizonsend.ion.server.features.sequences.SequenceManager
import net.horizonsend.ion.server.features.sequences.trigger.DataPredicate.DataPredicateSettings
import org.bukkit.entity.Player
import java.util.function.Predicate
import kotlin.jvm.optionals.getOrNull


object DataPredicate: SequenceTriggerType<DataPredicateSettings<*>>() {
	override fun setupChecks() {}

	class DataPredicateSettings<T : Any>(
		val dataTypeKey: String,
		val predicate: Predicate<T?>
	) : TriggerSettings() {
		override fun shouldProceed(player: Player, callingTrigger: SequenceTriggerType<*>): Boolean {
			val storedData = SequenceManager.getSequenceData(player).get<T>(dataTypeKey).getOrNull()
			return predicate.test(storedData)
		}
	}
}
