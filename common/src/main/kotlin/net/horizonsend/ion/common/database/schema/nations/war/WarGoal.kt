package net.horizonsend.ion.common.database.schema.nations.war

import java.util.Date
import java.util.concurrent.TimeUnit

enum class WarGoal(val verb: String, val truceEndDuration: Long) {
	HUMILIATE("to humiliate", TimeUnit.DAYS.toMillis(30)),
	END_THREAT("to end the threat of", TimeUnit.DAYS.toMillis(60)),
	WHITE_PEACE("", TimeUnit.DAYS.toMillis(30))

	;

	fun getTruceEndDate(warEndDate: Date): Date = Date(warEndDate.time + truceEndDuration)
}

