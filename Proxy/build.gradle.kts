plugins {
	id("org.jetbrains.kotlin.jvm")
}

repositories {
	mavenCentral()

	maven("https://repo.papermc.io/repository/maven-public/")
	maven("https://repo.aikar.co/content/groups/aikar/")
}

dependencies {
	compileOnly("io.github.waterfallmc:waterfall-api:1.19-R0.1-SNAPSHOT")

	implementation(project(":Common"))

	implementation("co.aikar:acf-bungee:0.5.1-SNAPSHOT")

	implementation("net.dv8tion:JDA:5.0.0-alpha.19") {
		exclude("opus-java")
	}
}