import io.papermc.paperweight.util.path
import java.io.ByteArrayOutputStream

plugins {
	kotlin("jvm")
	kotlin("plugin.serialization")

	id("com.github.johnrengelman.shadow")
	id("io.papermc.paperweight.userdev") version "2.0.0-beta.8"
}

repositories {
	mavenCentral()

	maven("https://repo.dmulloy2.net/repository/public/")
	maven("https://s01.oss.sonatype.org/content/repositories/snapshots/") // UnifiedMetrics
	maven("https://jitpack.io/") // Dynmap, Vault
	maven("https://m2.dv8tion.net/releases/") // JDA
	maven("https://maven.citizensnpcs.co/repo") // Citizens
	maven("https://nexus.scarsz.me/content/groups/public/") // DiscordSRV
	maven("https://repo.aikar.co/content/groups/aikar/") // ACF
	maven("https://repo.alessiodp.com/releases") // Libby (Required by Citizens)
	maven("https://repo.decalium.ru/releases") // Persistent Data Types
	maven("https://repo.xenondevs.xyz/releases") // InvUI
	maven("https://repo.papermc.io/repository/maven-public/") // FAWE api
	maven("https://maven.enginehub.org/repo/") // FAWE Alt
	maven("https://repo.codemc.io/repository/maven-snapshots/") // AnvilGUI

	maven("https://repo.horizonsend.net/mirror")
}

dependencies {
	implementation(project(":common"))

	compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
	// Platform
	paperweight.paperDevBundle("1.21.4-R0.1-SNAPSHOT")

	// Other Plugins
	compileOnly("com.github.webbukkit.dynmap:spigot:3.1") { exclude("*") /* Old Version, takes forever to download */ }
	compileOnly("net.citizensnpcs:citizens-main:2.0.30-SNAPSHOT") { exclude("*") }
	compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.2.8")
	compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
	compileOnly("com.discordsrv:discordsrv:1.29.0")
	compileOnly("net.luckperms:api:5.4")
	compileOnly("xyz.xenondevs.invui:invui:1.43") // Downloaded via paper library manager for remapping

	// Included Dependencies
	implementation("com.manya:persistent-data-types:1.0.25")
	implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")
	implementation("com.daveanthonythomas.moshipack:moshipack:1.0.1")
	implementation("com.github.stefvanschie.inventoryframework:IF:0.10.19")
	implementation("com.googlecode.cqengine:cqengine:3.6.0")
	implementation("fr.skytasul:guardianbeam:2.4.0")
	implementation("club.minnced:discord-webhooks:0.8.4")

	val scoreboardLibraryVersion = "2.2.2"
	implementation("net.megavex:scoreboard-library-extra-kotlin:$scoreboardLibraryVersion")
	implementation("net.megavex:scoreboard-library-api:$scoreboardLibraryVersion")
	implementation("net.megavex:scoreboard-library-implementation:$scoreboardLibraryVersion")
	implementation("net.megavex:scoreboard-library-modern:$scoreboardLibraryVersion:mojmap")

    implementation("org.jgrapht:jgrapht-core:1.5.2")
	implementation("dev.vankka:mcdiscordreserializer:4.3.0")
	implementation("org.apache.commons:commons-collections4:4.4")

	implementation(platform("com.intellectualsites.bom:bom-newest:1.51"))
	compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Core:2.12.2")

	compileOnly("dev.cubxity.plugins", "unifiedmetrics-api", "0.3.8")
}

paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION

kotlin.jvmToolchain(21)

tasks.withType<AbstractArchiveTask>().configureEach {
	isPreserveFileTimestamps = false
	isReproducibleFileOrder = true
}

val output = ByteArrayOutputStream()
project.exec {
	setCommandLine("git", "rev-parse", "--verify", "--short", "HEAD")
	standardOutput = output
}
val gitHash = String(output.toByteArray()).trim()

val embedHash = tasks.create("embedHash") {
	doLast {
		val buildDir = layout.buildDirectory.get().path.toAbsolutePath().toString()
		File("$buildDir/resources/main").mkdirs()
		File("$buildDir/resources/main/gitHash").writeText(gitHash)
	}
}

tasks.classes {
	dependsOn(embedHash)
}

tasks.shadowJar {
	archiveFileName.set("IonServer.jar")
	destinationDirectory.set(file(rootProject.projectDir.absolutePath + "/build"))
}

tasks.build {
	dependsOn(tasks.shadowJar)
}
