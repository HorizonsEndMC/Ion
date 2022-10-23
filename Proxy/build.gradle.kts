plugins {
	id("org.jetbrains.kotlin.jvm")
}

dependencies {
	compileOnly("io.github.waterfallmc:waterfall-api:1.19-R0.1-SNAPSHOT")

	compileOnly("com.github.NuVotifier.NuVotifier:nuvotifier-api:2.7.2")
	compileOnly("com.github.NuVotifier.NuVotifier:nuvotifier-bungeecord:2.7.2")

	implementation(project(":Common"))

	implementation("co.aikar:acf-bungee:0.5.1-SNAPSHOT")

	implementation("net.dv8tion:JDA:5.0.0-alpha.22") {
		exclude("opus-java")
	}
}