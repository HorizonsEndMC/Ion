plugins {
	kotlin("plugin.serialization")
}

repositories {
	mavenCentral()
}

dependencies {
	api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")

	api(libs.bundles.configurate)
	api(libs.bundles.kotlin)
	api(libs.jedis)
	api(libs.kmongo)
}
