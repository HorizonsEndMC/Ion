plugins {
	id("org.jetbrains.kotlin.jvm")
}

repositories {
	mavenCentral()

	maven("https://repo.aikar.co/content/groups/aikar/") // Annotation Command Framework
}

dependencies {
	// Adventure / MiniMessage
	// Velocity uses 4.10.1, Paper uses 4.11.0. As adventure follows semantic versioning,
	// there should be no issues as it is a minor release.
	compileOnly("net.kyori:adventure-api:4.10.1")
	compileOnly("net.kyori:adventure-text-minimessage:4.10.1")
	compileOnly("net.kyori:adventure-text-serializer-legacy:4.10.1")

	// LuckPerms API
	compileOnly("net.luckperms:api:5.4")

	// Configurate
	// Velocity provides 3.7.2, which is so out of date that we will just provide our own.
	api("org.spongepowered:configurate-hocon:4.1.0")
	api("org.spongepowered:configurate-extra-kotlin:4.1.0")

	// Annotation Command Framework
	api("co.aikar:acf-core:0.5.1-SNAPSHOT")

	// Kotlin Relfection
	api("org.jetbrains.kotlin:kotlin-reflect:1.7.10")
}