package net.horizonsend.ion.server.features.progression

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

object PrestigeCongratulations {
	private val WINDOW_MILLIS = TimeUnit.MINUTES.toMillis(5)
	private val entries = ConcurrentHashMap<UUID, Entry>()

	class Entry internal constructor(val timestamp: Long, val prestige: Int) {
		val congratulatedBy: MutableSet<UUID> = ConcurrentHashMap.newKeySet()

		fun isExpired(now: Long = System.currentTimeMillis()): Boolean = now - timestamp >= WINDOW_MILLIS
	}

	fun startWindow(playerId: UUID, prestige: Int) {
		entries[playerId] = Entry(System.currentTimeMillis(), prestige)
	}

	operator fun get(playerId: UUID): Entry? = entries[playerId]

	fun remove(playerId: UUID, entry: Entry) {
		entries.remove(playerId, entry)
	}
}