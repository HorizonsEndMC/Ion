package net.horizonsend.ion.common.database

import net.horizonsend.ion.common.database.types.oid
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption.CASCADE

class NationInvite(id: EntityID<Int>) : IntEntity(id) {
	var nation by Nation referencedOn Table.nation
	var settlement by Table.settlement

	companion object : IonEntityClass<Int, NationInvite>(Table, NationInvite::class.java, ::NationInvite)

	object Table : IntIdTable("nation_invites") {
		val nation = reference("nation", Nation.Table, onDelete = CASCADE)
		val settlement = oid<Any>("settlement")
	}
}
