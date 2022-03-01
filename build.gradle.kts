import org.gradle.jvm.toolchain.JavaLanguageVersion.of

plugins {
	kotlin("jvm") version "1.6.10"
	id("com.github.johnrengelman.shadow") version "7.1.2"
}

repositories {
	maven("https://oss.sonatype.org/content/repositories/snapshots/") // MiniMessage

	maven("https://papermc.io/repo/repository/maven-public/") // Paper API

	mavenCentral()
}

dependencies {
	compileOnly("io.papermc.paper:paper-api:1.18.1-R0.1-SNAPSHOT")

	compileOnly(project(":IonCore"))

	implementation("net.kyori:adventure-text-minimessage:4.10.0")
}

tasks {
	compileKotlin { kotlinOptions { jvmTarget = "17" } }
	shadowJar {
		minimize()
		archiveFileName.set("../Ion.jar")
	}
	build {
		dependsOn("shadowJar")
		dependsOn("IonCore:reobfJar")
	}
}

java.toolchain.languageVersion.set(of(17))