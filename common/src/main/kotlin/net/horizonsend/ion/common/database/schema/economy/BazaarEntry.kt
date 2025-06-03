package net.horizonsend.ion.common.database.schema.economy

import net.horizonsend.ion.common.database.DbObject
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.nations.Territory

interface BazaarEntry : DbObject {
	val itemString: String
	val cityTerritory: Oid<Territory>
}
