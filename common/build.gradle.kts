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
	compileOnly("net.kyori:adventure-api:4.13.1")
	compileOnly("net.kyori:adventure-text-logger-slf4j:4.13.1")
	compileOnly("net.kyori:adventure-text-minimessage:4.13.1")

	compileOnlyApi("com.mysql:mysql-connector-j:8.0.33")

	api(kotlin("reflect"))
	api(kotlin("stdlib"))

	api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
	api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.0")

	api("org.jetbrains.exposed:exposed-core:0.41.1")
	api("org.jetbrains.exposed:exposed-dao:0.41.1")
	api("org.jetbrains.exposed:exposed-jdbc:0.41.1")
	api("org.jetbrains.exposed:exposed-java-time:0.41.1")

	api("com.zaxxer:HikariCP:5.0.1")

	api("com.h2database:h2:2.1.214")

	api("redis.clients:jedis:4.3.2")
}

kotlin.jvmToolchain(17)
