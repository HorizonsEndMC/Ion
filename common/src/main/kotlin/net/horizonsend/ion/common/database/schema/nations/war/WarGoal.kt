package net.horizonsend.ion.common.database.schema.nations.war

import java.util.Date

interface WarGoal {
	fun getTruceEndDuration(): Long

	fun getTruceEndDate(warEndDate: Date): Date = Date(warEndDate.time + getTruceEndDuration())
}
