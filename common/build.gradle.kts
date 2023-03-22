plugins {
	kotlin("plugin.serialization")
}

repositories {
	mavenCentral()
}

dependencies {
	compileOnly("net.kyori:adventure-api:4.13.0")
	compileOnly(libs.bundles.paperProvided)

	api(libs.bundles.kotlin)

	api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
	api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

	api("org.jetbrains.exposed:exposed-core:0.41.1")
	api("org.jetbrains.exposed:exposed-dao:0.41.1")
	api("org.jetbrains.exposed:exposed-jdbc:0.41.1")
	api("org.jetbrains.exposed:exposed-java-time:0.41.1")

	api("com.zaxxer:HikariCP:5.0.1")

	api("org.mariadb.jdbc:mariadb-java-client:3.1.3")
	api("com.h2database:h2:2.1.214")

	api(libs.jedis)
}
