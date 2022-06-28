plugins {
	id("com.github.johnrengelman.shadow") version "7.1.2"
	id("org.jlleitschuh.gradle.ktlint") version "10.3.0"
	id("org.jetbrains.kotlin.jvm") version "1.7.0"
	id("xyz.jpenilla.run-paper") version "1.0.6"
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
		fun ionRelocate(`package`: String) {
			relocate(`package`, "net.horizonsend.ion.libraries.${`package`}")
		}

		arrayOf(
			"org.spongepowered.configurate",
			"org.intellij.lang.annotations",
			"org.jetbrains.annotations",
			"io.leangen.geantyref",
			"com.typesafe.config",
			"co.aikar.commands",
			"co.aikar.locales"
		).forEach { group ->
			ionRelocate(group)
		}

		archiveFileName.set("../Ion.jar")
	}

	runServer { minecraftVersion("1.19") }
	ktlint { version.set("0.44.0") }

	prepareKotlinBuildScriptModel { dependsOn("addKtlintFormatGitPreCommitHook") }
	addKtlintFormatGitPreCommitHook { dependsOn("shadowJar") }
	build { dependsOn("shadowJar") }
}