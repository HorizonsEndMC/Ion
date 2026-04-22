import groovy.json.JsonSlurper
import org.jetbrains.kotlin.gradle.internal.ensureParentDirsCreated
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL

plugins {
	id("com.gradleup.shadow") version "9.3.0" apply false

	kotlin("plugin.serialization") version "2.2.20" apply false
	kotlin("jvm") version "2.2.20" apply false
}

val serverPlugins = listOf(
	ServerPlugin(
		name = "FastAsyncWorldEdit",
		mode = PluginMode.REQUIRED,
		downloadType = DownloadType.GITHUB_RELEASE_TAG,
		tag = "2.15.0",
		jarName = "FastAsyncWorldEdit-Paper-2.15.0.jar",
		source = "IntellectualSites/FastAsyncWorldEdit",
		assetName = "FastAsyncWorldEdit-Paper-2.15.0.jar"
	),
	ServerPlugin(
		name = "UnifiedMetrics",
		mode = PluginMode.REQUIRED,
		downloadType = DownloadType.GITHUB_RELEASE_TAG,
		jarName = "unifiedmetrics-platform-bukkit-0.3.8.jar",
		source = "Cubxity/UnifiedMetrics",
		tag = "v0.3.8",
		assetName = "unifiedmetrics-platform-bukkit-0.3.8.jar"
	),
	ServerPlugin(
		name = "ProtocolLib",
		mode = PluginMode.REQUIRED,
		downloadType = DownloadType.GITHUB_RELEASE_TAG,
		tag = "5.4.0",
		jarName = "ProtocolLib.jar",
		source = "dmulloy2/ProtocolLib",
		assetName = "ProtocolLib.jar"
	),
	ServerPlugin(
		name = "Citizens",
		mode = PluginMode.EXTENDED,
		downloadType = DownloadType.DIRECT,
		jarName = "Citizens.jar",
		source = "https://ci.citizensnpcs.co/view/Citizens/job/Citizens2/4134/artifact/dist/target/Citizens-2.0.41-b4134.jar"
	),
	ServerPlugin(
		name = "LuckPerms",
		mode = PluginMode.REQUIRED,
		downloadType = DownloadType.JENKINS,
		jarName = "LuckPerms-Bukkit.jar",
		source = "https://ci.lucko.me/job/LuckPerms",
		filter = "Bukkit"
	),
	ServerPlugin(
		name = "Multiverse-Core",
		mode = PluginMode.EXTENDED,
		downloadType = DownloadType.GITHUB_RELEASE_TAG,
		tag = "5.5.3",
		jarName = "multiverse-core-5.5.3.jar",
		source = "Multiverse/Multiverse-Core",
		assetName = "multiverse-core-5.5.3.jar"
	),
	ServerPlugin(
		name = "LiteBans",
		mode = PluginMode.PRODUCTION,
		downloadType = DownloadType.DIRECT,
		jarName = "LiteBans.jar",
		source = "PUT_REAL_URL_HERE"
	)
)

val testServerPluginDir = rootProject.layout.projectDirectory.dir("run/paper/plugins").asFile

fun httpGetText(url: String): String {
	val connection = (URL(url).openConnection() as HttpURLConnection).apply {
		requestMethod = "GET"
		setRequestProperty("Accept", "application/json")
		connectTimeout = 15_000
		readTimeout = 15_000
	}

	return connection.inputStream.bufferedReader().use { it.readText() }
}

fun downloadToFile(url: String, target: File) {
	target.parentFile.mkdirs()

	logger.lifecycle("Downloading $url -> ${target.absolutePath}")

	URL(url).openStream().use { input ->
		target.outputStream().use { output ->
			input.copyTo(output)
		}
	}
}

fun downloadGithubReleaseAsset(
	repo: String,
	tag: String,
	assetName: String,
	target: File
) {
	val releaseApiUrl = "https://api.github.com/repos/$repo/releases/tags/$tag"
	val json = httpGetText(releaseApiUrl)

	val parsed = JsonSlurper().parseText(json) as Map<*, *>
	val assets = parsed["assets"] as? List<*> ?: error("No assets found for $repo@$tag")

	val asset = assets
		.map { it as Map<*, *> }
		.firstOrNull { it["name"] == assetName }
		?: error("Could not find GitHub release asset '$assetName' for $repo@$tag")

	val assetUrl = asset["browser_download_url"] as? String
		?: error("Asset '$assetName' did not have a browser_download_url")

	downloadToFile(assetUrl, target)
}


fun downloadJenkinsLastSuccessfulArtifact(
	jobBaseUrl: String,
	artifactFilter: String,
	target: File,
	relativePathContains: String? = null
) {
	val apiUrl = "$jobBaseUrl/lastSuccessfulBuild/api/json"
	val json = httpGetText(apiUrl)

	val fileNameRegex = Regex(""""fileName"\s*:\s*"([^"]+)"""")
	val relativePathRegex = Regex(""""relativePath"\s*:\s*"([^"]+)"""")

	val artifacts = fileNameRegex.findAll(json).map { it.groupValues[1] }
		.zip(relativePathRegex.findAll(json).map { it.groupValues[1] })
		.toList()

	val artifactPath = artifacts.firstOrNull { (fileName, relativePath) ->
		fileName.endsWith(".jar", ignoreCase = true) &&
			fileName.contains(artifactFilter, ignoreCase = true) &&
			(relativePathContains == null || relativePath.contains(relativePathContains, ignoreCase = true))
	}?.second ?: error("No Jenkins artifact matched '$artifactFilter' from $jobBaseUrl")

	val downloadUrl = "$jobBaseUrl/lastSuccessfulBuild/artifact/$artifactPath"
	downloadToFile(downloadUrl, target)
}

fun downloadModrinthVersion(
	projectIdOrSlug: String,
	target: File,
	versionType: String? = null,
	gameVersion: String? = null,
	loader: String = "paper"
) {
	val params = mutableListOf<String>()
	params += "loaders=[\"$loader\"]"

	if (gameVersion != null) {
		params += "game_versions=[\"$gameVersion\"]"
	}

	if (versionType != null) {
		params += "featured=${versionType == "release"}"
	}

	val apiUrl = buildString {
		append("https://api.modrinth.com/v2/project/")
		append(projectIdOrSlug)
		append("/version?")
		append(params.joinToString("&"))
	}

	val json = httpGetText(apiUrl)

	val urlRegex = Regex(""""url"\s*:\s*"([^"]+\.jar)"""")
	val firstJarUrl = urlRegex.find(json)?.groupValues?.get(1)
		?: error("No Modrinth jar found for project '$projectIdOrSlug'")

	downloadToFile(firstJarUrl, target)
}

fun deleteOldPluginVariants(pluginDir: File, plugin: ServerPlugin) {
	if (!pluginDir.exists()) return

	val desiredName = plugin.jarName.lowercase()
	val desiredStem = plugin.jarName.substringBeforeLast(".jar").lowercase()

	pluginDir.listFiles()
		.orEmpty()
		.filter { it.isFile && it.extension.equals("jar", ignoreCase = true) }
		.filter { existing ->
			val name = existing.name.lowercase()
			name == desiredName ||
				name.startsWith("$desiredStem-") ||
				name.startsWith("${plugin.name.lowercase()}-") ||
				name == "${plugin.name.lowercase()}.jar"
		}
		.forEach { old ->
			logger.lifecycle("Deleting old plugin jar ${old.name}")
			old.delete()
		}
}

fun downloadPlugins(plugins: List<ServerPlugin>) {
	testServerPluginDir.mkdirs()

	plugins.forEach { plugin ->
		val target = File(testServerPluginDir, plugin.jarName)

		deleteOldPluginVariants(testServerPluginDir, plugin)

		when (plugin.downloadType) {
			DownloadType.DIRECT -> {
				downloadToFile(plugin.source, target)
			}

			DownloadType.GITHUB_RELEASE_TAG -> {
				downloadGithubReleaseAsset(
					repo = plugin.source,
					tag = plugin.tag ?: error("Missing tag for ${plugin.name}"),
					assetName = plugin.assetName ?: error("Missing assetName for ${plugin.name}"),
					target = target
				)
			}

			DownloadType.JENKINS -> {
				downloadJenkinsLastSuccessfulArtifact(
					jobBaseUrl = plugin.source,
					artifactFilter = plugin.filter ?: error("Missing filter for ${plugin.name}"),
					target = target
				)
			}

			DownloadType.MODRINTH_VERSION -> {
				downloadModrinthVersion(
					projectIdOrSlug = plugin.source,
					target = target,
					gameVersion = plugin.gameVersion,
					loader = plugin.loader ?: "paper"
				)
			}

		}
	}
}


fun pluginsFor(mode: PluginMode): List<ServerPlugin> = when (mode) {
	PluginMode.REQUIRED ->
		serverPlugins.filter { it.mode == PluginMode.REQUIRED }

	PluginMode.EXTENDED ->
		serverPlugins.filter { it.mode == PluginMode.REQUIRED || it.mode == PluginMode.EXTENDED }

	PluginMode.PRODUCTION ->
		serverPlugins
}

tasks.register("downloadRequiredServerPlugins") {
	doLast { downloadPlugins(pluginsFor(PluginMode.REQUIRED)) }
}

tasks.register("downloadExtendedServerPlugins") {
	doLast { downloadPlugins(pluginsFor(PluginMode.EXTENDED)) }
}

tasks.register("downloadProductionServerPlugins") {
	doLast { downloadPlugins(pluginsFor(PluginMode.PRODUCTION)) }
}

tasks.register("downloadTestServerDependencies") {
	group = "test server"
	doLast {
		downloadPlugins(pluginsFor(PluginMode.EXTENDED))
	}
}

enum class PluginMode {
	REQUIRED,
	EXTENDED,
	PRODUCTION
}

enum class DownloadType {
	DIRECT,
	MODRINTH_VERSION,
	GITHUB_RELEASE_TAG,
	JENKINS
}

data class ServerPlugin(
	val name: String,
	val mode: PluginMode,
	val downloadType: DownloadType,
	val jarName: String,
	val source: String,
	val assetName: String? = null,
	val filter: String? = null,
	val tag: String? = null,
	val gameVersion: String? = null,
	val loader: String? = null
)
