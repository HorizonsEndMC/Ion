import net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder

plugins {
	id("xyz.jpenilla.run-paper") version "1.0.6" // Run Paper
	id("org.jetbrains.kotlin.jvm") version "1.7.0" // Kotlin
	id("io.papermc.paperweight.userdev") version "1.3.7" // Paperweight
	id("com.github.johnrengelman.shadow") version "7.1.2" // ShadowJar
	id("org.jlleitschuh.gradle.ktlint") version "10.3.0" // KTLint
	id("net.minecrell.plugin-yml.bukkit") version "0.5.2" // Plugin-YML
}

repositories {
	mavenCentral()

	maven("https://repo.papermc.io/repository/maven-public/") // Paper

	maven("https://repo.aikar.co/content/groups/aikar/") // Annotation Command Framework
}

dependencies {
	paperDevBundle("1.19-R0.1-SNAPSHOT") // Paper

	// Provided by other Plugins
	compileOnly(project(":IonCore")) // IonCore

	// Provided by us
	implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT") // Annotation Command Framework
}

bukkit {
	main = "net.horizonsend.ion.server.IonServer"
	apiVersion = "1.19"
	load = PluginLoadOrder.STARTUP
}

tasks {
	compileJava {
		options.compilerArgs.add("-parameters")
		options.isFork = true
	}

	compileKotlin {
		kotlinOptions.javaParameters = true
		kotlinOptions.jvmTarget = "17"
	}

	shadowJar {
		archiveFileName.set("../Ion.jar")

		relocate("co.aikar.commands", "net.horizonsend.ion.libraries.co.aikar.commands")
		relocate("co.aikar.locales", "net.horizonsend.ion.libraries.co.aikar.locales")
	}

	runServer {
		minecraftVersion("1.19")
	}

	build {
		dependsOn(":shadowJar")
		dependsOn(":IonCore:build")
	}

	ktlint {
		version.set("0.44.0")
	}

	prepareKotlinBuildScriptModel {
		dependsOn("addKtlintCheckGitPreCommitHook")
		dependsOn("addKtlintFormatGitPreCommitHook")
	}
}