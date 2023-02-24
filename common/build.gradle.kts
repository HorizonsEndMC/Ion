plugins {
	kotlin("plugin.serialization")
}

repositories {
	mavenCentral()
}

dependencies {
	api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")

	api(libs.bundles.kotlin)
	api(libs.jedis)
	api(libs.kmongo)
}
