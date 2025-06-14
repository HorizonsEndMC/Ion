import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
	id("com.github.johnrengelman.shadow")

	kotlin("kapt")
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
	compileOnly("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")
	kapt("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")

	compileOnly("net.luckperms:api:5.4")

	// Included Dependencies
	implementation("co.aikar:acf-velocity:0.5.1-SNAPSHOT")
	implementation("net.dv8tion:JDA:5.6.1")
	implementation("net.kyori:adventure-text-logger-slf4j:4.21.0")
	implementation("dev.vankka:mcdiscordreserializer:4.3.0")
}

tasks.build { dependsOn("shadowJar") }
tasks.shadowJar { archiveFileName.set("../../../build/IonProxy.jar") }

tasks.named<KotlinJvmCompile>("compileKotlin") {
	compilerOptions {
		javaParameters = true
	}
}

kotlin.jvmToolchain(21)

tasks.withType<AbstractArchiveTask>().configureEach {
	isPreserveFileTimestamps = false
	isReproducibleFileOrder = true
}
