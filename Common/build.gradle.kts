plugins {
	id("org.jetbrains.kotlin.jvm")
}

dependencies {
	api("org.spongepowered:configurate-hocon:4.1.2")
	api("org.spongepowered:configurate-extra-kotlin:4.1.2")

	api("co.aikar:acf-core:0.5.1-SNAPSHOT")

	api("org.jetbrains.kotlin:kotlin-reflect:1.7.20")

	implementation("org.litote.kmongo:kmongo:4.7.2")
}