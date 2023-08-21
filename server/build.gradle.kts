import java.io.ByteArrayOutputStream

plugins {
	id("io.papermc.paperweight.userdev") version "1.5.5"
	id("com.github.johnrengelman.shadow")

	kotlin("plugin.serialization")
	kotlin("jvm")
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
	maven("https://repo.decalium.ru/releases")
	maven("https://repo.xenondevs.xyz/releases") // InvUI

	maven("https://repo.horizonsend.net/mirror")
}

dependencies {
	implementation(project(":common"))

	compileOnly("io.papermc.paper:paper-api:1.19.4-R0.1-20230607.154752-103")
	// Platform
	paperweight.paperDevBundle("1.19.4-R0.1-SNAPSHOT")

	// Other Plugins
	compileOnly("com.github.webbukkit.dynmap:spigot:3.1") { exclude("org.bukkit") /* Old Version */ }
	compileOnly("com.comphenix.protocol:ProtocolLib:5.1.0")
	compileOnly("net.citizensnpcs:citizens-main:2.0.30-SNAPSHOT")
	compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.2.8")
	compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
	compileOnly("com.discordsrv:discordsrv:1.25.1")
	compileOnly("net.luckperms:api:5.4")

	// Included Dependencies
	implementation("com.manya:persistent-data-types:1.0.25")
	implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")
	implementation("com.daveanthonythomas.moshipack:moshipack:1.0.1")
	implementation("com.github.stefvanschie.inventoryframework:IF:0.10.10")
	implementation("com.googlecode.cqengine:cqengine:3.6.0")
	implementation("io.github.config4k:config4k:0.6.0")
	implementation("net.wesjd:anvilgui:1.6.3-SNAPSHOT")
	implementation("io.github.skytasul:guardianbeam:2.3.3")
	implementation("xyz.xenondevs.invui:invui:1.14")
	implementation("club.minnced:discord-webhooks:0.8.4")
	implementation("com.github.megavexnetwork.scoreboard-library:scoreboard-library-extra-kotlin:2.0.0-RC11")
	implementation("com.github.megavexnetwork.scoreboard-library:scoreboard-library-api:2.0.0-RC11")
	implementation("com.github.megavexnetwork.scoreboard-library:scoreboard-library-implementation:2.0.0-RC12")
	implementation("com.github.megavexnetwork.scoreboard-library:scoreboard-library-modern:2.0.0-RC11")

	compileOnly("dev.cubxity.plugins", "unifiedmetrics-api", "0.3.8")
}

tasks.reobfJar { outputJar.set(file(rootProject.projectDir.absolutePath + "/build/IonServer.jar")) }
tasks.build { dependsOn("reobfJar") }

kotlin.jvmToolchain(17)

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
		File("$buildDir/resources/main").mkdirs()
		File("$buildDir/resources/main/gitHash").writeText(gitHash)
	}
}

tasks.classes {
	dependsOn(embedHash)
}
