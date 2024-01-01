package net.horizonsend.ion.common.database.cache.nations

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.cache.ManualCache
import net.horizonsend.ion.common.database.schema.nations.war.ActiveWar
import net.horizonsend.ion.common.database.schema.nations.war.WarGoal
import java.util.Date

class WarCache : ManualCache() {
	data class WarData(
		val id: Oid<ActiveWar>,

		val name: String?,

		val aggressor: Oid<ActiveWar>,
		val defender: Oid<ActiveWar>,

		val startDate: Date,
		var endDate: Date?,

		val goal: WarGoal,
		var points: Int,
		var ended: Boolean
	)

	override fun load() {

	}

	fun findActive() {

	}
}
