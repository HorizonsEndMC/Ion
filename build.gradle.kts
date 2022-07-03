import java.net.URL

plugins {
	id("com.github.johnrengelman.shadow") version "7.1.2"
	id("org.jlleitschuh.gradle.ktlint") version "10.3.0"
	id("org.jetbrains.kotlin.jvm") version "1.7.0"
}

repositories {
	mavenCentral()

	maven("https://repo.aikar.co/content/groups/aikar/") // Annotation Command Framework
}

dependencies {
	implementation(project(":Server"))
	implementation(project(":Proxy"))
}

tasks {
	shadowJar {
		archiveFileName.set("../Ion.jar")
	}

	ktlint { version.set("0.44.0") }

	prepareKotlinBuildScriptModel { dependsOn("addKtlintFormatGitPreCommitHook") }
	addKtlintFormatGitPreCommitHook { dependsOn("shadowJar") }
	build { dependsOn("shadowJar") }

	create("downloadTestServerDependencies") {
		fun downloadJenkinsArtifact(domain: String, project: String, filter: String, location: String, destination: String) {
			val jarName = URL("https://$domain/job/$project/lastSuccessfulBuild/api/xml?xpath=/freeStyleBuild/artifact/relativePath[$filter]")
				.readText()
				.substringAfter("<relativePath>$location/")
				.substringBefore("</relativePath>")

			print("Downloading $jarName... ")

			File("./$destination/plugins")
				.apply { mkdirs() }
				.resolve(jarName)
				.writeBytes(
					URL("https://$domain/job/$project/lastSuccessfulBuild/artifact/$location/$jarName")
						.readBytes()
				)

			println("Done!")
		}

		doFirst {
			downloadJenkinsArtifact("ci.athion.net", "FastAsyncWorldEdit", "contains(.,'Bukkit')", "artifacts", "paper")
			downloadJenkinsArtifact("ci.lucko.me", "LuckPerms", "starts-with(.,'bukkit/')", "bukkit/loader/build/libs", "paper")
			downloadJenkinsArtifact("ci.lucko.me", "LuckPerms", "starts-with(.,'velocity/')", "velocity/build/libs", "velocity")
		}
	}
}