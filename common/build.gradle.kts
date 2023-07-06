plugins {
	kotlin("plugin.serialization")
	kotlin("jvm")

	`java-library`
}

repositories {
	mavenCentral()

	maven("https://repo.horizonsend.net/mirror")
}

dependencies {
	compileOnly("net.kyori:adventure-api:4.14.0")
	compileOnly("net.kyori:adventure-text-logger-slf4j:4.14.0")
	compileOnly("net.kyori:adventure-text-minimessage:4.14.0")

	api(kotlin("reflect"))
	api(kotlin("stdlib"))

	api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
	api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.2")

	api("org.litote.kmongo:kmongo-shared:4.9.0")
	api("org.mongodb:bson:4.10.1")

	api("redis.clients:jedis:4.4.3")
}

kotlin.jvmToolchain(17)
