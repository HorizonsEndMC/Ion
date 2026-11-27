package net.horizonsend.ion.server.features.economy.bazaar

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.common.database.schema.economy.BazaarOrder
import net.horizonsend.ion.common.utils.input.InputResult
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.RED
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

object BazaarGuard {
	private val held: MutableSet<Any> = ConcurrentHashMap.newKeySet()

	fun orderKey(id: Oid<BazaarOrder>): Any = "order:$id"

	fun listingKey(id: Oid<BazaarItem>): Any = "listing:$id"

	fun playerKey(id: UUID): Any = "player:$id"

	fun tryAcquire(key: Any): Boolean = held.add(key)

	fun release(key: Any) {
		held.remove(key)
	}

	val BUSY: InputResult = InputResult.FailureReason(
		listOf(text("That is still being processed, please wait a moment!", RED))
	)

	class Release(private val key: Any) {
		private val done = AtomicBoolean(false)

		operator fun invoke() {
			if (done.compareAndSet(false, true)) release(key)
		}
	}
}