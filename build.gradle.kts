import groovy.json.JsonSlurper
import org.jetbrains.kotlin.gradle.internal.ensureParentDirsCreated
import java.net.URI

plugins {
	id("com.github.johnrengelman.shadow") version "8.1.1" apply false

	kotlin("plugin.serialization") version "2.2.20" apply false
	kotlin("jvm") version "2.2.20" apply false
}

// TODO: Use Json
fun downloadJenkinsArtifact(domain: String, project: String, filter: String, location: String) {
	val jarName = URI("https://$domain/job/$project/lastSuccessfulBuild/api/xml?xpath=/freeStyleBuild/artifact/relativePath${if (filter.isNotEmpty()) "[$filter]" else ""}").toURL()
		.readText()
		.substringAfter("<relativePath>$location/")
		.substringBefore("</relativePath>")

	logger.log(LogLevel.QUIET, "Downloading $jarName... ")

	val projectDir = layout.buildDirectory.get().asFile.parentFile.absolutePath

	val file = File("$projectDir/run/paper/plugins/$jarName")

	if (!file.exists()) {
		file.ensureParentDirsCreated()
		file.createNewFile()
	} else {
		logger.log(LogLevel.QUIET, "Plugin already present, skipping.")
		return
	}

	file.writeBytes(URI("https://$domain/job/$project/lastSuccessfulBuild/artifact/$location/$jarName").toURL().readBytes())

	logger.log(LogLevel.QUIET, "Done!")
}

@Suppress("UNCHECKED_CAST")
fun downloadModrinthArtifact(project: String, targetVersion: String = "1.21.11") {
	val targetLoader = "paper"

	logger.log(LogLevel.QUIET, "Downloading $project... ")

	val versions = JsonSlurper().parseText(URI("https://api.modrinth.com/v2/project/$project/version").toURL().readText()) as ArrayList<Map<String, Any>>

	val version = versions.firstOrNull check@{ version ->
		val gameVersions = version["game_versions"] as ArrayList<String>
		val gameLoaders = version["loaders"] as ArrayList<String>

		if (gameVersions.none { it == targetVersion }) return@check false
		if (gameLoaders.none { it == targetLoader }) return@check false

		true
	}

	if (version == null) {
		logger.log(LogLevel.ERROR, "\nCould not find suitable version for $project!")
		return
	}

	val file = (version["files"] as ArrayList<Map<String, Any>>).first()["url"] as String
	val fileName = file.substringAfterLast("/")

	val projectDir = layout.buildDirectory.get().asFile.parentFile.absolutePath

	val pluginDestinationFile = File("$projectDir/run/paper/plugins/$fileName")

	if (!pluginDestinationFile.exists()) {
		pluginDestinationFile.ensureParentDirsCreated()
		pluginDestinationFile.createNewFile()
	} else {
		logger.log(LogLevel.QUIET, "Plugin already present, skipping.")
		return
	}

	pluginDestinationFile.writeBytes(URI(file).toURL().readBytes())

	logger.log(LogLevel.QUIET, "Done!")
}

tasks.register("downloadTestServerDependencies") {
	doFirst {
		downloadJenkinsArtifact("ci.athion.net", "FastAsyncWorldEdit", "contains(.,'Bukkit')", "artifacts")
		downloadJenkinsArtifact("ci.lucko.me", "LuckPerms", "starts-with(.,'bukkit/')", "bukkit/loader/build/libs")
		downloadModrinthArtifact("unifiedmetrics", targetVersion = "1.19.4")
	}
}
