plugins {
	application

	id("com.github.johnrengelman.shadow")

	kotlin("plugin.serialization")
	kotlin("jvm")
}

repositories {
	mavenCentral()
	maven("https://m2.dv8tion.net/releases/")
}

dependencies {
	implementation(project(":common"))

	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.2")
	implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.1")

	implementation("org.slf4j:slf4j-api:1.7.5")
	implementation("org.slf4j:slf4j-simple:1.7.5")

	implementation("net.dv8tion:JDA:5.0.0-beta.13") { exclude("opus-java") }
}

application {
	mainClass.set("net.horizonsend.ion.discord.EntrypointKt")
}

tasks.register<Wrapper>("wrapper") {
	gradleVersion = "8.5"
}

tasks.shadowJar {
	destinationDirectory.set(layout.buildDirectory.get())
	archiveFileName.set("IonDiscordBot.jar")
}

tasks.build {
	dependsOn(tasks.shadowJar)
}

kotlin.jvmToolchain(17)
