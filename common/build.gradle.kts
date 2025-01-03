plugins {
	kotlin("plugin.serialization")
	kotlin("jvm")

	`java-library`
}

repositories {
	mavenCentral()

	maven("https://repo.horizonsend.net/mirror")
	maven("https://jitpack.io/")
}

dependencies {
	compileOnly("net.kyori:adventure-api:4.18.0")
	compileOnly("net.kyori:adventure-text-logger-slf4j:4.18.0")
	compileOnly("net.kyori:adventure-text-minimessage:4.18.0")
	compileOnly("net.kyori:adventure-text-serializer-plain:4.18.0")
	compileOnly("net.kyori:adventure-text-serializer-gson:4.18.0")
	compileOnly("net.kyori:adventure-text-serializer-legacy:4.17.0")

	compileOnly("net.md-5:bungeecord-chat:1.20-R0.2")
	compileOnly("net.luckperms:api:5.4")
	compileOnly("com.gitlab.ruany:LiteBansAPI:0.5.0")

	api(kotlin("reflect"))
	api(kotlin("stdlib"))

	api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
	api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
	api("com.googlecode.cqengine:cqengine:3.6.0")

	api("org.litote.kmongo:kmongo:5.1.0")
	api("com.fasterxml.jackson.core:jackson-databind:2.18.2")

	api("redis.clients:jedis:5.2.0")
}

kotlin.jvmToolchain(17)
