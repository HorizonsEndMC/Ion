plugins {
	kotlin("plugin.serialization")
}

dependencies {
	compileOnly(libs.waterfall) // Platform

	compileOnly(libs.bundles.nuvotifier) // Other Plugin
	compileOnly(libs.luckperms) // Other Plugin

	// Included
	implementation(libs.acfBungee)
	implementation(libs.jda)
	implementation(libs.bundles.miniMessageBungee)
	implementation(libs.bundles.paperProvided)
}

tasks.shadowJar { archiveFileName.set("../../../build/IonProxy.jar") }
