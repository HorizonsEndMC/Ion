import java.net.URL

plugins {
	id("com.github.johnrengelman.shadow") version "7.1.2"
	id("io.papermc.paperweight.userdev") version "1.3.8"
	id("org.jlleitschuh.gradle.ktlint") version "11.0.0"
	id("org.jetbrains.kotlin.jvm") version "1.7.10"
	id("xyz.jpenilla.run-paper") version "1.0.6"
}

repositories {
	mavenCentral()

	maven("https://repo.papermc.io/repository/maven-public/")
	maven("https://repo.aikar.co/content/groups/aikar/")
}

dependencies {
	paperDevBundle("1.19.2-R0.1-SNAPSHOT") // Userdev needs to be on the root module otherwise run-paper won't work.

	implementation(project(":Server"))
	implementation(project(":Proxy"))
}

tasks {
	reobfJar {
		outputJar.set(file(rootProject.projectDir.absolutePath + "/build/Ion.jar"))
	}

	runServer { minecraftVersion("1.19.2") }
	ktlint { version.set("0.44.0") }

	prepareKotlinBuildScriptModel { dependsOn("addKtlintFormatGitPreCommitHook") }
	addKtlintFormatGitPreCommitHook { dependsOn("shadowJar") }
	build { dependsOn("reobfJar") }

	create("downloadTestServerDependencies") {
		fun downloadJenkinsArtifact(
			domain: String,
			project: String,
			filter: String,
			location: String,
			destination: String
		) {
			val jarName =
				URL("https://$domain/job/$project/lastSuccessfulBuild/api/xml?xpath=/freeStyleBuild/artifact/relativePath[$filter]")
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
			downloadJenkinsArtifact(
				"ci.lucko.me",
				"LuckPerms",
				"starts-with(.,'bukkit/')",
				"bukkit/loader/build/libs",
				"paper"
			)
			downloadJenkinsArtifact(
				"ci.lucko.me",
				"LuckPerms",
				"starts-with(.,'velocity/')",
				"velocity/build/libs",
				"velocity"
			)
		}
	}

	compileKotlin {
		kotlinOptions {
			jvmTarget = "17"
		}
	}

	compileJava {
		sourceCompatibility = "17"
		targetCompatibility = "17"
	}
}