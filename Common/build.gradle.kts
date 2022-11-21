plugins {
	id("org.jetbrains.kotlin.jvm")
	id("com.diffplug.spotless")
}

dependencies {
	api("org.spongepowered:configurate-extra-kotlin:4.1.2")
	api("org.spongepowered:configurate-hocon:4.1.2")

	api("org.jetbrains.kotlin:kotlin-reflect:1.7.21")
	api("co.aikar:acf-core:0.5.1-SNAPSHOT")

	implementation("org.litote.kmongo:kmongo:4.7.2")
	implementation("redis.clients:jedis:4.3.1")
}