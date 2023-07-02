import java.io.ByteArrayOutputStream

plugins {
	id("com.github.johnrengelman.shadow")

	kotlin("plugin.serialization")
	kotlin("jvm")
}

repositories {
	mavenCentral()

	maven("https://mvn.exceptionflug.de/repository/exceptionflug-public/") // Protocolize
	maven("https://repo.aikar.co/content/groups/aikar/") // ACF
	maven("https://repo.papermc.io/repository/maven-public/") // Waterfall
	maven("https://jitpack.io/")
}

dependencies {
	implementation(project(":common"))

	// Platform
	compileOnly("com.velocitypowered:velocity-api:3.1.1")

	// Other Plugins
	compileOnly("net.luckperms:api:5.4")
	compileOnly("dev.simplix:protocolize-api:2.3.0")

	// Included Dependencies
	implementation("net.dv8tion:JDA:5.0.0-beta.12")
	implementation("com.github.minndevelopment:jda-ktx:0.10.0-beta.1")
	implementation("net.kyori:adventure-text-logger-slf4j:4.14.0")
	implementation("net.kyori:adventure-text-minimessage:4.14.0")
}

tasks.build { dependsOn("shadowJar") }
tasks.shadowJar { archiveFileName.set("../../../build/IonProxy.jar") }

kotlin.jvmToolchain(17)

tasks.withType<AbstractArchiveTask>().configureEach {
	isPreserveFileTimestamps = false
	isReproducibleFileOrder = true
}
