package net.horizonsend.web

import io.jooby.ExecutionMode.EVENT_LOOP
import io.jooby.OpenAPIModule
import io.jooby.jackson.JacksonModule
import io.jooby.kt.runApp
import io.jooby.whoops.WhoopsModule
import net.horizonsend.ion.common.CommonConfig
import net.horizonsend.ion.common.database.DBManager
import net.horizonsend.ion.common.utils.redisaction.RedisActions
import org.reflections.Reflections
import java.io.File

fun main(args: Array<String>) {
	val reflections = Reflections("net.horizonsend.web")

	CommonConfig.init(File("configs/"))
	val components = listOf(DBManager, RedisActions)
	components.forEach { it.onEnable() }

	runApp(args, EVENT_LOOP) {
		install(WhoopsModule())
		install(JacksonModule())
		install(OpenAPIModule())

		reflections.getTypesAnnotatedWith(Register::class.java).forEach {
			mvc(it.getDeclaredConstructor().newInstance())
		}
	}

	Runtime.getRuntime().addShutdownHook(Thread {
		components.reversed().forEach { it.onDisable() }
	})
}

annotation class Register
