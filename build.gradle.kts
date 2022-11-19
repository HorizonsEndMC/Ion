import java.net.URL

plugins {
	id("com.github.johnrengelman.shadow") version "7.1.2"
	id("io.papermc.paperweight.userdev") version "1.3.9"
	id("com.diffplug.spotless") version "6.11.0"
	kotlin("jvm") version "1.7.21"
}

allprojects {
	repositories {
		mavenCentral()

		maven("https://repo.codemc.io/repository/maven-snapshots/")
		maven("https://repo.papermc.io/repository/maven-public/")
		maven("https://nexus.scarsz.me/content/groups/public/")
		maven("https://repo.aikar.co/content/groups/aikar/")
		maven("https://repo.alessiodp.com/releases")
		maven("https://maven.citizensnpcs.co/repo")
		maven("https://m2.dv8tion.net/releases")
		maven("https://jitpack.io")
	}
}

dependencies {
	paperDevBundle("1.19.2-R0.1-SNAPSHOT")

	implementation(project(":Proxy"))
	implementation(project(":Server"))
}

tasks.reobfJar {
	outputJar.set(file(rootProject.projectDir.absolutePath + "/build/Ion.jar"))
}

tasks.build {
	dependsOn(":Server:reobfJar")
	dependsOn(":spotlessApply")
	dependsOn("shadowJar")
}

spotless {
	kotlin {
		ktlint("0.47.1")

		trimTrailingWhitespace()
		indentWithTabs()
		endWithNewline()
	}
}

kotlin.jvmToolchain(17)

// TODO: Use Json
// TODO: Don't redownload every time
fun downloadJenkinsArtifact(domain: String, project: String, filter: String, location: String) {
	val jarName =
		URL("https://$domain/job/$project/lastSuccessfulBuild/api/xml?xpath=/freeStyleBuild/artifact/relativePath[$filter]")
			.readText()
			.substringAfter("<relativePath>$location/")
			.substringBefore("</relativePath>")

	print("Downloading $jarName... ")

	File("./run/paper/plugins/$jarName")
		.writeBytes(
			URL("https://$domain/job/$project/lastSuccessfulBuild/artifact/$location/$jarName")
				.readBytes()
		)

	println("Done!")
}

tasks.create("downloadTestServerDependencies") {
	doFirst {
		downloadJenkinsArtifact("ci.athion.net", "FastAsyncWorldEdit", "contains(.,'Bukkit')", "artifacts")
		downloadJenkinsArtifact("ci.lucko.me", "LuckPerms", "starts-with(.,'bukkit/')", "bukkit/loader/build/libs")
	}
}