package net.horizonsend.web.utils

import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.redis
import java.util.UUID

val NOT_FOUND = "{\"ok\": false, \"message\": \"Not Found\"}"

fun validateApiKey(uuid: UUID) = redis {
	if (exists("$uuid")) {
		return@redis true
	} else if (SLPlayer.isApiKeyValid(uuid)) {
		set("$uuid", "")

		return@redis true
	} else {
		return@redis false
	}
}

