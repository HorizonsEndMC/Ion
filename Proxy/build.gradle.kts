plugins {
	id("com.github.johnrengelman.shadow") version "7.1.2"

	id("com.diffplug.spotless")
	kotlin("jvm")
}

repositories {
	mavenCentral()

	maven("https://repo.horizonsend.net/mirror") // Horizon's End Mirror

	maven("https://repo.papermc.io/repository/maven-public/") // Waterfall
	maven("https://repo.aikar.co/content/groups/aikar/") // Annotation Command Framework (Bungeecord)
	maven("https://jitpack.io") // NuVotifier
}

dependencies {
	implementation(project(":Common"))

	// Platform
	compileOnly("io.github.waterfallmc:waterfall-api:1.19-R0.1-SNAPSHOT")

	// Plugin Dependencies
	compileOnly("com.github.NuVotifier.NuVotifier:nuvotifier-bungeecord:2.7.2")
	compileOnly("com.github.NuVotifier.NuVotifier:nuvotifier-api:2.7.2")

	// Included Dependencies
	implementation("co.aikar:acf-bungee:0.5.1-SNAPSHOT")

	// Library Loaded Dependencies
	compileOnly("net.dv8tion:JDA:5.0.0-beta.2")

	// Common Library Loaded Dependencies
	compileOnly("org.spongepowered:configurate-extra-kotlin:4.1.2")
	compileOnly("org.spongepowered:configurate-hocon:4.1.2")

	compileOnly("org.jetbrains.kotlin:kotlin-reflect:1.7.22")
	compileOnly("org.jetbrains.kotlin:kotlin-stdlib:1.7.22")

	compileOnly("org.litote.kmongo:kmongo:4.8.0")
	compileOnly("redis.clients:jedis:4.3.1")
}

tasks.shadowJar { archiveFileName.set("../../../build/IonProxy.jar") }

tasks.build {
	dependsOn("spotlessApply")
	dependsOn("shadowJar")
}