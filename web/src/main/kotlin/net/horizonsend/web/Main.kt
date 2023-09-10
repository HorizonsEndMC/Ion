package net.horizonsend.web

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import io.jooby.*
import io.jooby.ExecutionMode.EVENT_LOOP
import io.jooby.exception.StatusCodeException
import io.jooby.handler.AccessLogHandler
import io.jooby.handler.CorsHandler
import io.jooby.handler.RateLimitHandler
import io.jooby.jackson.JacksonModule
import io.jooby.kt.runApp
import io.jooby.whoops.WhoopsModule
import net.horizonsend.ion.common.CommonConfig
import net.horizonsend.ion.common.database.DBManager
import net.horizonsend.ion.common.utils.redisaction.RedisActions
import net.horizonsend.web.utils.MUST_AUTHENTICATE
import net.horizonsend.web.utils.validateApiKey
import org.reflections.Reflections
import java.io.File
import java.time.Duration
import java.util.*

fun main(args: Array<String>) {
	val reflections = Reflections("net.horizonsend.web")

	CommonConfig.init(File("configs/"))
	val components = listOf(DBManager, RedisActions)
	components.forEach { it.onEnable() }

	runApp(args, EVENT_LOOP) {
		install(WhoopsModule())
		install(JacksonModule())
		install(OpenAPIModule())

		use(AccessLogHandler())
		use(ReactiveSupport.concurrent())
		use(CorsHandler())

		router.isTrustProxy = true

		val rateLimit = mutableListOf<Any>()
		reflections.getTypesAnnotatedWith(Register::class.java).forEach {
			if (it.isAnnotationPresent(Secure::class.java)) {
				rateLimit.add(it.getDeclaredConstructor().newInstance())
				return@forEach
			}

			mvc(it.getDeclaredConstructor().newInstance())
		}

		// Authentication + RateLimiting pipeline
		use {
			if (!ctx.requestPath.startsWith("/v1")) return@use next.apply(ctx)

			val stringKey =
				ctx.header("Authorization").valueOrNull() ?: throw StatusCodeException(StatusCode.UNAUTHORIZED, MUST_AUTHENTICATE)
			val key = runCatching { UUID.fromString(stringKey) }.getOrNull()
				?: throw StatusCodeException(StatusCode.BAD_REQUEST, MUST_AUTHENTICATE)

			if (!validateApiKey(key)) throw StatusCodeException(StatusCode.UNAUTHORIZED, MUST_AUTHENTICATE)

			return@use next.apply(ctx)
		}

		before(RateLimitHandler({
			val limit = Bandwidth.simple(60, Duration.ofMinutes(1))
			Bucket.builder().addLimit(limit).build() as Bucket
		}, "Authorization"))

		rateLimit.forEach {
			mvc(it)
		}
	}

	Runtime.getRuntime().addShutdownHook(Thread {
		components.reversed().forEach { it.onDisable() }
	})
}

annotation class Register
annotation class Secure
