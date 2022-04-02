import org.gradle.jvm.toolchain.JavaLanguageVersion.of

plugins {
  java
  kotlin("jvm") version "1.6.20"
	id("io.papermc.paperweight.userdev") version "1.3.5"
  id("com.github.johnrengelman.shadow") version "7.1.2"
}

repositories {
	mavenCentral()
	maven("https://jitpack.io")
	maven("https://repo.citizensnpcs.co/")
	maven("https://maven.sk89q.com/repo/")
	maven("https://m2.dv8tion.net/releases") // JDA (Required by DiscordSRV)
	maven("https://www.myget.org/F/egg82-java/maven/")
	maven("https://repo.aikar.co/content/groups/aikar/")
	maven("https://nexus.scarsz.me/content/groups/public/")
	maven("https://papermc.io/repo/repository/maven-public/")
	maven("https://nexus.vankka.dev/repository/maven-public/")
	maven("https://repo.glaremasters.me/repository/bloodshot") // GriefDefender
	maven("https://repo.codemc.io/repository/maven-snapshots/")
	maven("https://repo.codemc.io/repository/maven-snapshots/")
	maven("https://oss.sonatype.org/content/repositories/snapshots/")
	maven("https://raw.githubusercontent.com/FabioZumbi12/UltimateChat/mvn-repo/")
}

val minecraftVersion = "1.18.2-R0.1-SNAPSHOT"

dependencies {
	// Dynmap and probably something else is including old Bukkit / Spigot versions, I could use excludes...
	// or I can just set paper at the top, thus overriding it. - Peter
	compileOnly("io.papermc.paper:paper-api:$minecraftVersion")
	paperDevBundle(minecraftVersion)

	compileOnly("net.luckperms:api:5.4")
	compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
	compileOnly("net.citizensnpcs:citizens:2.0.27-SNAPSHOT")
	compileOnly("com.github.webbukkit.dynmap:spigot:3.1")
	compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.2.8")

	implementation("org.jetbrains.kotlin:kotlin-reflect:1.6.20")
	implementation("org.litote.kmongo:kmongo:4.5.1")
	implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")

	// TODO: Remove these
	compileOnly("com.discordsrv:discordsrv:1.25.1")
	compileOnly("com.griefdefender:api:2.1.0-SNAPSHOT")

	implementation("org.ejml:ejml-all:0.41")
	implementation("com.github.jkcclemens:khttp:0.1.0")
	implementation("io.github.config4k:config4k:0.4.2")
	implementation("net.wesjd:anvilgui:1.5.3-SNAPSHOT")
	implementation("club.minnced:discord-webhooks:0.8.0")
	implementation("ninja.egg82:event-chain-bukkit:1.0.7")
	implementation("com.googlecode.cqengine:cqengine:3.6.0")
	implementation("com.daveanthonythomas.moshipack:moshipack:1.0.1")

	// Older versions need to be used for compatibility, I won't bother fixing it for now because were going to drop these
	// dependencies in the future anyway - Peter
	implementation("redis.clients:jedis:3.7.1")
	implementation("com.github.stefvanschie.inventoryframework:IF:0.5.8")
}

tasks {
	compileKotlin { kotlinOptions { jvmTarget = "17" } }
	reobfJar { outputJar.set(file(rootProject.projectDir.absolutePath + "/build/IonCore.jar")) }
}

java.toolchain.languageVersion.set(of(17))