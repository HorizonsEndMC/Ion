plugins {
	kotlin("plugin.serialization")
}

repositories {
	mavenCentral()
}

dependencies {
	api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
	api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

	api(libs.bundles.kotlin)
	api(libs.jedis)
	api(libs.kmongo)
}
