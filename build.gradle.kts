import org.gradle.jvm.toolchain.JavaLanguageVersion.of

plugins {
	kotlin("jvm") version "1.6.20"
	id("com.github.johnrengelman.shadow") version "7.1.2"
}

repositories {
	maven("https://papermc.io/repo/repository/maven-public/") // Paper API
	maven("https://repo.aikar.co/content/groups/aikar/") // Annotation Command Framework
	mavenCentral()
}

val configurateVersion = "4.1.2"

dependencies {
	compileOnly("io.papermc.paper:paper-api:1.18.2-R0.1-SNAPSHOT") // Paper

	compileOnly(project(":IonCore")) // IonCore

	implementation("org.spongepowered:configurate-hocon:$configurateVersion") // Configurate (HOCON)
	implementation("org.spongepowered:configurate-extra-kotlin:$configurateVersion") // Configurate (Kotlin Additions)

	implementation("org.jetbrains.kotlin:kotlin-reflect:1.6.20") // Required by Configurate (Kotlin Additions)

	implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT") // Annotation Command Framework
}

tasks {
	compileKotlin { kotlinOptions { jvmTarget = "17" } }
	shadowJar {
		archiveFileName.set("../Ion.jar")
	}
	build {
		dependsOn("shadowJar")
		dependsOn("IonCore:reobfJar")
	}
}

java.toolchain.languageVersion.set(of(17))