package net.horizonsend.ion.server.features.sequences

import net.horizonsend.ion.server.miscellaneous.registrations.persistence.MetaDataContainer
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.PDCSerializers
import net.kyori.adventure.text.Component
import java.util.Optional
import java.util.PriorityQueue

class SequenceDataStore(val keyedData: MutableMap<String, Any> = mutableMapOf(), val context: SequenceContext) {
	val metaDataMirror = mutableMapOf<String, MetaDataContainer<*, *>>()

	data class QueuedMessage(val scheduledTick: Long, val insertionOrder: Long, val message: Component) : Comparable<QueuedMessage> {
		override fun compareTo(other: QueuedMessage): Int {
			val tickCompare = this.scheduledTick.compareTo(other.scheduledTick)
			if (tickCompare != 0) return tickCompare
			return this.insertionOrder.compareTo(other.insertionOrder)
		}
	}

	private var messageInsertionCounter: Long = 0
	val messageQueue: PriorityQueue<QueuedMessage> = PriorityQueue()

	fun queueDelayedMessage(scheduledTick: Long, message: Component) {
		messageQueue.add(QueuedMessage(scheduledTick, messageInsertionCounter++, message))
	}

	operator fun <T : Any> get(key: String): Optional<T> {
		if (!keyedData.containsKey(key)) return Optional.empty<T>()
		@Suppress("UNCHECKED_CAST")
		return Optional.ofNullable(keyedData[key] as? T)
	}

	fun <T : Any> set(key: String, value: T) {
		metaDataMirror[key] = PDCSerializers.pack(value)
		keyedData[key] = value
	}
}
