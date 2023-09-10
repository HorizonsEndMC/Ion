package net.horizonsend.web.utils

import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.redis
import java.util.UUID

const val NOT_FOUND = "{\"ok\": false, \"message\": \"Not Found\"}"
const val MUST_AUTHENTICATE = "{\"ok\": false, \"message\": \"This endpoint requires authorization.\"}"

fun validateApiKey(uuid: UUID): Boolean = if (System.getenv("DEV") != null) true
else redis {
	if (exists("$uuid")) {
		return@redis true
	} else if (SLPlayer.isApiKeyValid(uuid)) {
		set("$uuid", "")

		return@redis true
	} else {
		return@redis false
	}
}

