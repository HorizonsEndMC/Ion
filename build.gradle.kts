import org.gradle.jvm.toolchain.JavaLanguageVersion.of

plugins {
  java
	id("com.github.johnrengelman.shadow") version "7.1.2" // ShadowJar
	id("io.papermc.paperweight.userdev") version "1.3.6"  // Paperweight
	kotlin("jvm") version "1.6.21"                        // Kotlin
	kotlin("plugin.serialization") version "1.6.21"       // Kotlin Serialization
}

repositories {
	mavenCentral()

	maven("https://repo.codemc.io/repository/maven-snapshots/") // AnvilGUI
	maven("https://nexus.scarsz.me/content/groups/public/") // DiscordSRV
	maven("https://repo.aikar.co/content/groups/aikar/") // Annotation Command Framework (Paper), WorldEdit API
	maven("https://m2.dv8tion.net/releases") // JDA (Required by DiscordSRV)
	maven("https://repo.citizensnpcs.co/") // Citizens
	maven("https://jitpack.io") // khttp, VaultAPI, Dynmap (Spigot)
}

dependencies {
	paperDevBundle("1.18.2-R0.1-SNAPSHOT")

	// Provided by us
		implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")

		// TODO: Remove
			implementation("com.github.jkcclemens:khttp:0.1.0") {
				exclude("org.jetbrains.kotlin") // Old Version + Provided by Server Library Loader
				exclude("org.json") // Server Library Loader
			}
			implementation("net.wesjd:anvilgui:1.5.3-SNAPSHOT")

	// Provided by other plugins
		compileOnly("net.luckperms:api:5.4")
		compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
		compileOnly("net.citizensnpcs:citizens:2.0.27-SNAPSHOT")
		compileOnly("com.github.webbukkit.dynmap:spigot:3.1") {
			exclude("org.bukkit") // Old Version
		}
		compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.2.8")

		// TODO: Remove
			compileOnly("com.discordsrv:discordsrv:1.25.1")

	// Provided by Server Library Loader
		compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
		compileOnly("org.jetbrains.kotlin:kotlin-reflect:1.6.21")
		compileOnly("org.jetbrains.kotlin:kotlin-stdlib:1.6.21")
		compileOnly("org.litote.kmongo:kmongo:4.5.1")

		// TODO: Remove
			compileOnly("io.github.config4k:config4k:0.4.2")
			compileOnly("com.googlecode.cqengine:cqengine:3.6.0")
			compileOnly("com.daveanthonythomas.moshipack:moshipack:1.0.1")

			// Older Versions
				compileOnly("redis.clients:jedis:3.7.1")
				compileOnly("com.github.stefvanschie.inventoryframework:IF:0.5.8")
}

tasks {
	compileJava {
		options.compilerArgs.add("-parameters")
	}

	compileKotlin {
		kotlinOptions.javaParameters = true
		kotlinOptions.jvmTarget = "17"
	}

	shadowJar {
		relocate("co.aikar.commands", "net.horizonsend.ion.core.libraries.co.aikar.commands")
		relocate("co.aikar.locales", "net.horizonsend.ion.core.libraries.co.aikar.locales")
		relocate("net.wesjd.anvilgui", "net.horizonsend.ion.core.libraries.net.wesjd.anvilgui")
	}

	reobfJar {
		outputJar.set(file(rootProject.projectDir.absolutePath + "/build/IonCore.jar"))
	}
}

java.toolchain.languageVersion.set(of(17))