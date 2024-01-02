package net.horizonsend.ion.common.database.schema.nations.war

import java.util.Date
import java.util.concurrent.TimeUnit

interface WarGoal {
	fun getVerb(): String

	fun getTruceEndDuration(): Long

	fun getTruceEndDate(warEndDate: Date): Date = Date(warEndDate.time + getTruceEndDuration())
}

data object Humiliate : WarGoal {
	override fun getVerb(): String = "to humiliate"

	override fun getTruceEndDuration(): Long = TimeUnit.DAYS.toMillis(30)
}

data object EndThreat : WarGoal {
	override fun getVerb(): String = "to end the threat of"

	override fun getTruceEndDuration(): Long = TimeUnit.DAYS.toMillis(60)
}

data object WhitePeace : WarGoal {
	override fun getVerb(): String = ""

	override fun getTruceEndDuration(): Long = TimeUnit.DAYS.toMillis(30)
}

