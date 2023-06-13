package net.horizonsend.ion.common.database

import net.horizonsend.ion.common.database.types.oid
import net.starlegacy.database.schema.nations.MoneyHolder
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.litote.kmongo.id.WrappedObjectId

class Nation(id: EntityID<Int>) : IntEntity(id), MoneyHolder {
	@Deprecated("")
	var objectId by Table.objectId

	var name by Table.name
	var color by Table.color

	var capital by Table.capital

	var _balance by Table.balance

	@Deprecated("")
	override var balance: Int
		get() = transaction { _balance }
		set(value) = transaction { _balance = value }

	companion object : IonEntityClass<Int, Nation>(Table, Nation::class.java, ::Nation) {
		fun getByName(name: String) = find(Table.name eq name).firstOrNull()

		operator fun get(objectId: WrappedObjectId<Nation>) = find(Table.objectId eq objectId).firstOrNull()
	}

	object Table : IntIdTable("nations") {
		@Deprecated("")
		val objectId = oid<Nation>("object_id")

		val name = varchar("name", 32).uniqueIndex()
		val color = integer("color").uniqueIndex()

		val capital = oid<Any>("capital")

		val balance = integer("balance")
	}
}
