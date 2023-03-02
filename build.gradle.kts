import java.net.URL

@Suppress("DSL_SCOPE_VIOLATION") // TODO: https://youtrack.jetbrains.com/issue/KTIJ-19369
plugins {
	kotlin("plugin.serialization") version "1.8.10" apply false
	alias(libs.plugins.shadow) apply false
	alias(libs.plugins.spotless)
	alias(libs.plugins.kotlin)
}

allprojects {
	apply(plugin = rootProject.libs.plugins.spotless.get().pluginId)
	apply(plugin = rootProject.libs.plugins.kotlin.get().pluginId)

	if (name != "common" && rootProject != this) {
		apply(plugin = rootProject.libs.plugins.shadow.get().pluginId)

		dependencies { implementation(project(":common")) }

		tasks.build {
			dependsOn("shadowJar")
		}
	}

	repositories {
		mavenCentral()

		maven("https://repo.horizonsend.net/mirror")

		maven("https://repo.papermc.io/repository/maven-public/") // Waterfall
		maven("https://repo.aikar.co/content/groups/aikar/") // Annotation Command Framework (Paper)
		maven("https://repo.alessiodp.com/releases") // Libby (Required by Citizens)
		maven("https://repo.codemc.io/repository/maven-snapshots/") // WorldEdit
		maven("https://nexus.scarsz.me/content/groups/public/") // DiscordSRV
		maven("https://s01.oss.sonatype.org/content/repositories/snapshots") // DiscordSRV Depends
		maven("https://jitpack.io") // Dynmap (Spigot), Vault, NuVotifier, GuardianBeam
		maven("https://maven.citizensnpcs.co/repo") // Citizens
		maven("https://m2.dv8tion.net/releases/")
	}

	kotlin.jvmToolchain(17)

	tasks.build {
		dependsOn("spotlessApply")
	}

	spotless {
		kotlin {
			ktlint("0.47.1").editorConfigOverride(mapOf(
				"ktlint_disabled_rules" to arrayOf(
					"annotation", // Inlining annotations is cleaner sometimes
					"argument-list-wrapping" // This seems to break arbitrarily with MenuHelper.kt, come back to this
				).joinToString()
			))

			trimTrailingWhitespace()
			indentWithTabs()
			endWithNewline()
		}
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
