package net.horizonsend.ion.common.database.schema.nations.war

import java.util.concurrent.TimeUnit

data object Humiliate : WarGoal {
	override fun getTruceEndDuration(): Long = TimeUnit.DAYS.toMillis(30)
}
