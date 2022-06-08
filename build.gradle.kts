plugins {
	id("xyz.jpenilla.run-paper") version "1.0.6" // Run Paper
	id("org.jetbrains.kotlin.jvm") version "1.7.0" // Kotlin
	id("io.papermc.paperweight.userdev") version "1.3.6" // Paperweight
	id("com.github.johnrengelman.shadow") version "7.1.2" // ShadowJar
	id("org.jlleitschuh.gradle.ktlint") version "10.3.0" // KTLint
}

repositories {
	mavenCentral()

	maven("https://repo.aikar.co/content/groups/aikar/") // Annotation Command Framework
}

dependencies {
	paperDevBundle("1.18.2-R0.1-SNAPSHOT") // Paper

	// Provided by other Plugins
	compileOnly(project(":IonCore")) // IonCore

	// Provided by us
	implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT") // Annotation Command Framework

	// Provided by Library Loader
	compileOnly("org.jetbrains.kotlin:kotlin-stdlib:1.6.21")
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
		minecraftVersion("1.18.2")
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