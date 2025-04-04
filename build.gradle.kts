import org.jetbrains.kotlin.js.parser.sourcemaps.JsonArray
import org.jetbrains.kotlin.js.parser.sourcemaps.JsonObject
import org.jetbrains.kotlin.js.parser.sourcemaps.JsonString
import org.jetbrains.kotlin.js.parser.sourcemaps.parseJson
import java.net.URI

plugins {
	id("com.github.johnrengelman.shadow") version "8.1.1" apply false

	kotlin("plugin.serialization") version "2.0.20" apply false
	kotlin("jvm") version "2.0.21" apply false
}

// TODO: Use Json
// TODO: Don't redownload every time
fun downloadJenkinsArtifact(domain: String, project: String, filter: String, location: String) {
	val jarName = URI("https://$domain/job/$project/lastSuccessfulBuild/api/xml?xpath=/freeStyleBuild/artifact/relativePath${if (filter.isNotEmpty()) "[$filter]" else ""}").toURL()
			.readText()
			.substringAfter("<relativePath>$location/")
			.substringBefore("</relativePath>")

	print("Downloading $jarName... ")

	File("./run/paper/plugins/$jarName")
		.writeBytes(
			URI("https://$domain/job/$project/lastSuccessfulBuild/artifact/$location/$jarName").toURL()
				.readBytes()
		)

	println("Done!")
}

fun downloadModrinthArtifact(project: String, targetVersion: String = "1.21.4") {
	val targetLoader = "paper"

	print("Downloading $project... ")

	val versions : JsonArray = parseJson(URI("https://api.modrinth.com/v2/project/$project/version").toURL().readText()) as JsonArray

	val version = versions.elements.firstOrNull check@{ version ->
		version as JsonObject

		val gameVersions = version.properties["game_versions"] as JsonArray
		val gameLoaders = version.properties["loaders"] as JsonArray

		if (gameVersions.elements.none { it as JsonString; it.value == targetVersion }) return@check false
		if (gameLoaders.elements.none { it as JsonString; it.value == targetLoader }) return@check false

		true
	} as JsonObject?

	if (version == null) {
		println("\nCould not find suitable version for $project!")
		return
	}

	val file = ((version.properties["files"] as JsonArray).elements[0] as JsonObject).properties["url"] as JsonString
	val fileName = file.value.substringAfterLast("/")

	File("./run/paper/plugins/$fileName").writeBytes(URI(file.value).toURL().readBytes())

	println("Done!")
}

tasks.register("downloadTestServerDependencies") {
	doFirst {
		downloadJenkinsArtifact("ci.athion.net", "FastAsyncWorldEdit", "contains(.,'Bukkit')", "artifacts")
		downloadJenkinsArtifact("ci.lucko.me", "LuckPerms", "starts-with(.,'bukkit/')", "bukkit/loader/build/libs")
		downloadModrinthArtifact("unifiedmetrics", targetVersion = "1.19.4")
	}
}
