package net.horizonsend.ion.common.database

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.UUID

class Cryopod(id: EntityID<Int>) : IntEntity(id) {
	var location by Table.location
	var owner by PlayerData referencedOn Table.owner
	var active by Table.active

	companion object : IonEntityClass<Int, Cryopod>(Table, Cryopod::class.java, ::Cryopod) {
		operator fun get(location: DBLocation) =
			Cryopod.find(Table.location eq location).firstOrNull()

		operator fun get(uuid: UUID) =
			Cryopod.find(Table.owner eq uuid)
	}

	object Table : IntIdTable("cryopods") {
		val location = location()
		val owner = reference("owner", PlayerData.Table, ReferenceOption.CASCADE)
		val active = bool("active")
	}
}
