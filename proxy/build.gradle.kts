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
	compileOnly("io.github.waterfallmc:waterfall-api:1.19-R0.1-SNAPSHOT")
	compileOnly("net.luckperms:api:5.4")

	// Included Dependencies
	implementation("co.aikar:acf-bungee:0.5.1-SNAPSHOT")
	implementation("net.dv8tion:JDA:5.0.0-beta.24")
	implementation("net.kyori:adventure-platform-bungeecord:4.3.2")
	implementation("net.kyori:adventure-text-logger-slf4j:4.17.0")

	implementation("net.kyori:adventure-text-minimessage:4.17.0")
	implementation("net.kyori:adventure-text-serializer-plain:4.17.0")

	implementation("dev.vankka:mcdiscordreserializer:4.3.0")
}

tasks.build { dependsOn("shadowJar") }
tasks.shadowJar { archiveFileName.set("../../../build/IonProxy.jar") }

kotlin.jvmToolchain(17)

tasks.withType<AbstractArchiveTask>().configureEach {
	isPreserveFileTimestamps = false
	isReproducibleFileOrder = true
}
