plugins {
	id("com.github.johnrengelman.shadow") version "7.1.2"
	id("org.jlleitschuh.gradle.ktlint") version "10.3.0"
	id("org.jetbrains.kotlin.jvm") version "1.7.0"
	id("xyz.jpenilla.run-paper") version "1.0.6"
}

repositories {
	mavenCentral()
}

dependencies {
	implementation(project(":Server"))
	implementation(project(":Proxy"))
}

tasks {
	shadowJar {
		arrayOf(
			"org.spongepowered.configurate",
			"org.intellij.lang.annotations",
			"org.jetbrains.annotations",
			"io.leangen.geantyref",
			"com.typesafe.config",
			"kotlin"
		).forEach { group ->
			relocate(group, "net.horizonsend.ion.common.libraries.$group")
		}

		archiveFileName.set("../Ion.jar")
	}

	runServer { minecraftVersion("1.19") }
	ktlint { version.set("0.44.0") }

	prepareKotlinBuildScriptModel { dependsOn("addKtlintFormatGitPreCommitHook") }
	addKtlintFormatGitPreCommitHook { dependsOn("shadowJar") }
	build { dependsOn("shadowJar") }
}