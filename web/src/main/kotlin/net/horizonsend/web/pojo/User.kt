package net.horizonsend.web.pojo

import com.fasterxml.jackson.annotation.JsonIgnore
import io.jooby.StatusCode
import io.jooby.exception.StatusCodeException
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.web.utils.NOT_FOUND

class User(username: String) {
	@Transient
	@JsonIgnore
	val db = SLPlayer[username] ?: throw StatusCodeException(StatusCode.NOT_FOUND, NOT_FOUND)

	val username = db.lastKnownName
	val lastSeen = db.lastSeen
	val xp = db.xp
	val level = db.level
	val achievements = db.achievements
	val bounty = db.bounty

	val nationId = db.nation
	val settlementId = db.settlement
}
