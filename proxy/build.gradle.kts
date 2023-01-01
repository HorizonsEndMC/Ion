dependencies {
	compileOnly(libs.waterfall) // Platform

	compileOnly(libs.bundles.nuvotifier) // Other Plugin

	// Library Loaded
	compileOnly(libs.jda)
	implementation(libs.acfBungee)
}

tasks.shadowJar { archiveFileName.set("../../../build/IonProxy.jar") }
