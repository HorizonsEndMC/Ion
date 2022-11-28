plugins {
	id("com.diffplug.spotless")
	kotlin("jvm")
}

repositories {
	mavenCentral()
}

dependencies {
	// Common Library Loaded Dependencies
	compileOnly("org.spongepowered:configurate-extra-kotlin:4.1.2")
	compileOnly("org.spongepowered:configurate-hocon:4.1.2")

	compileOnly("org.jetbrains.kotlin:kotlin-reflect:1.7.22")
	compileOnly("org.jetbrains.kotlin:kotlin-stdlib:1.7.22")

	compileOnly("org.litote.kmongo:kmongo:4.8.0")
	compileOnly("redis.clients:jedis:4.3.1")
}

tasks.build {
	dependsOn("spotlessApply")
}