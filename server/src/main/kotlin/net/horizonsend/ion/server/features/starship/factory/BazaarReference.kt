package net.horizonsend.ion.server.features.starship.factory

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.economy.BazaarItem

data class BazaarReference(val price: Double, var amount: Int, val id: Oid<BazaarItem>) {
	fun consume(consumeAmount: Int): Boolean {
		if (amount < consumeAmount) return false
		kotlin.runCatching { BazaarItem.removeStock(id, consumeAmount) }.onFailure { return false }
		amount -= consumeAmount
		return true
	}
}
