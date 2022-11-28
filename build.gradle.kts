import java.net.URL

plugins {
	id("com.diffplug.spotless") version "6.12.0"
	kotlin("jvm") version "1.7.22"
}

repositories {
	mavenCentral()
}

kotlin.jvmToolchain(17)

spotless {
	kotlin {
		ktlint("0.47.1")

		trimTrailingWhitespace()
		indentWithTabs()
		endWithNewline()
	}
}

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