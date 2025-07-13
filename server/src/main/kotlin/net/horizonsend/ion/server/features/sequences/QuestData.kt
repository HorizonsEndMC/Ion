package net.horizonsend.ion.server.features.sequences

import net.horizonsend.ion.server.miscellaneous.registrations.persistence.MetaDataContainer
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.PDCSerializers
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.PairType
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

data class QuestData(
	val currentPhase: String,
	val sequenceData: Map<String, MetaDataContainer<*, *>>
) {
	fun unpackDataStore(): SequenceDataStore {
		return SequenceDataStore(sequenceData.mapValuesTo(mutableMapOf()) { PDCSerializers.unpack(it.value) })
	}

	companion object : PersistentDataType<PersistentDataContainer, QuestData> {
		private val PHASE = NamespacedKeys.key("phase")
		private val DATA = NamespacedKeys.key("data")

		private val KEYED_CONTAINER_TYPE = PairType.pairListType(
			PersistentDataType.STRING,
			MetaDataContainer
		)

		override fun getPrimitiveType(): Class<PersistentDataContainer> = PersistentDataContainer::class.java
		override fun getComplexType(): Class<QuestData> = QuestData::class.java


		override fun toPrimitive(
			complex: QuestData,
			context: PersistentDataAdapterContext,
		): PersistentDataContainer {
			val pdc = context.newPersistentDataContainer()

			pdc.set(PHASE, PersistentDataType.STRING, complex.currentPhase)
			pdc.set(DATA, KEYED_CONTAINER_TYPE, complex.sequenceData.toList())

			return pdc
		}

		override fun fromPrimitive(
			primitive: PersistentDataContainer,
			context: PersistentDataAdapterContext,
		): QuestData {
			return QuestData(
				primitive.get(PHASE, PersistentDataType.STRING)!!,
				primitive.get(DATA, KEYED_CONTAINER_TYPE)!!.toMap()
			)
		}
	}
}
