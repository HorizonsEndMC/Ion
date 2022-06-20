plugins {
	id("org.jetbrains.kotlin.jvm")
}

repositories {
	mavenCentral()
}

dependencies {
	// Adventure / MiniMessage
	// Velocity uses 4.10.1, Paper uses 4.11.0. As adventure follows semantic versioning,
	// there should be no issues as it is a minor release.
	compileOnly("net.kyori:adventure-api:4.10.1")
	compileOnly("net.kyori:adventure-text-minimessage:4.10.1")

	// Configurate
	// Velocity provides 3.7.2, which is so out of date that we will just provide our own.
	implementation("org.spongepowered:configurate-hocon:4.1.0")
	implementation("org.spongepowered:configurate-extra-kotlin:4.1.0")
}