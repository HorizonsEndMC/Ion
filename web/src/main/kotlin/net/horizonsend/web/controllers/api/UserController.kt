package net.horizonsend.web.controllers.api

import io.jooby.annotation.GET
import io.jooby.annotation.Path
import io.jooby.annotation.PathParam
import net.horizonsend.web.Secure
import net.horizonsend.web.Register
import net.horizonsend.web.data.User

@Register
@Secure
@Path("/v1")
class UserController {
	@GET("/user/{username}")
	fun user(@PathParam username: String) = object {
		val ok = true
		val result = User(username)
	}
}
