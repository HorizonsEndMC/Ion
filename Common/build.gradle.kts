plugins {
	id("com.diffplug.spotless")
	kotlin("jvm")
}

repositories {
	mavenCentral()

	maven("https://repo.horizonsend.net/mirror") // Horizon's End Mirror
}

dependencies {
	// Common Library Loaded Dependencies
	compileOnly("org.spongepowered:configurate-extra-kotlin:4.1.2")
	compileOnly("org.spongepowered:configurate-hocon:4.1.2")

	compileOnly("org.jetbrains.kotlin:kotlin-reflect:1.8.0")
	compileOnly("org.jetbrains.kotlin:kotlin-stdlib:1.8.0")

	compileOnly("org.litote.kmongo:kmongo:4.8.0")
	compileOnly("redis.clients:jedis:4.3.1")
}

tasks.build {
	dependsOn("spotlessApply")
}