import org.gradle.jvm.toolchain.JavaLanguageVersion.of

plugins {
	kotlin("jvm") version "1.6.21"
	id("com.github.johnrengelman.shadow") version "7.1.2"
}

repositories {
	maven("https://papermc.io/repo/repository/maven-public/") // Paper API
	maven("https://repo.aikar.co/content/groups/aikar/") // Annotation Command Framework
	mavenCentral()
}

dependencies {
	compileOnly("io.papermc.paper:paper-api:1.18.2-R0.1-SNAPSHOT") // Paper

	// Provided by other Plugins
	compileOnly(project(":IonCore")) // IonCore

	// Provided by us
	implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT") // Annotation Command Framework

	// Provided by Library Loader
	compileOnly("org.jetbrains.kotlin:kotlin-stdlib:1.6.20")
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
}

java.toolchain.languageVersion.set(of(17))