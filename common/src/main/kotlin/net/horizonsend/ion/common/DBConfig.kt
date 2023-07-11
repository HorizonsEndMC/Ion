package net.horizonsend.ion.common

import kotlinx.serialization.Serializable
import java.io.File

object CommonConfig {
	lateinit var db: DBConfig
	lateinit var redis: Redis

	fun init(folder: File) {
		db = Configuration.load(folder, "db.json")
		redis = Configuration.load(folder, "redis.json")
	}
}

@Serializable
data class DBConfig(
	val host: String = "mongo",
	val port: Int = 27017,
	val database: String = "test",
	val username: String = "test",
	val password: String = "test"
)

data class Redis(
	val host: String = "redis",
	val channel: String = "starlegacytest"
)
