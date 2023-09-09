package net.horizonsend.web.controllers

import io.jooby.Context
import io.jooby.annotation.GET
import io.jooby.annotation.Path
import io.jooby.annotation.QueryParam
import net.horizonsend.web.Register
import net.horizonsend.web.utils.validateApiKey
import java.util.UUID

@Register
@Path("/")
class IndexController {
	@GET
	fun index(ctx: Context) {
		ctx.sendRedirect("/swagger")
	}

	@GET("checkApiKey")
	fun checkApiKey(@QueryParam key: UUID) = object {
		val valid = validateApiKey(key)
	}
}
