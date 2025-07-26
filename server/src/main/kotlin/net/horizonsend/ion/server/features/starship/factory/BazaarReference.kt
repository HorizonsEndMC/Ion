package net.horizonsend.ion.server.features.starship.factory

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import java.util.concurrent.atomic.AtomicInteger

data class BazaarReference(val string: String, val price: Double, val amount: AtomicInteger, val id: Oid<BazaarItem>) {
	fun consume(consumeAmount: Int): Boolean {
		if (amount.get() < consumeAmount) return false
		amount.addAndGet(-consumeAmount)
		return true
	}
}
