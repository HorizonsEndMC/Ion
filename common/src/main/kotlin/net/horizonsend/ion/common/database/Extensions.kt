package net.horizonsend.ion.common.database

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.sql.transactions.transaction

fun <T : Entity<*>> T.update(block: T.() -> Unit): T {
	transaction { block(this@update) }
	return this
}
