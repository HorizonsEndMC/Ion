import org.gradle.jvm.toolchain.JavaLanguageVersion.of

plugins {
  java
  kotlin("jvm") version "1.6.10"
	id("io.papermc.paperweight.userdev") version "1.3.4"
  id("com.github.johnrengelman.shadow") version "7.1.2"
}

repositories {
	jcenter()
	mavenCentral()
	maven("https://jitpack.io")
	maven("https://repo.citizensnpcs.co/")
	maven("https://maven.sk89q.com/repo/")
	maven("https://www.myget.org/F/egg82-java/maven/")
	maven("https://repo.aikar.co/content/groups/aikar/")
	maven("https://nexus.scarsz.me/content/groups/public/")
	maven("https://papermc.io/repo/repository/maven-public/")
	maven("https://nexus.vankka.dev/repository/maven-public/")
	maven("https://repo.codemc.io/repository/maven-snapshots/")
	maven("https://repo.codemc.io/repository/maven-snapshots/")
	maven("https://oss.sonatype.org/content/repositories/snapshots/")
	maven("https://raw.githubusercontent.com/FabioZumbi12/UltimateChat/mvn-repo/")
}

dependencies {
	compileOnly("io.papermc.paper:paper-api:1.18.1-R0.1-SNAPSHOT") // Dynmap includes Bukkit causing conflicts
	paperDevBundle("1.18.1-R0.1-SNAPSHOT")

	compileOnly("net.luckperms:api:5.4")
	compileOnly("com.github.MilkBowl:VaultAPI:1.7")
	compileOnly("net.citizensnpcs:citizens:2.0.27-SNAPSHOT")
	compileOnly("com.github.webbukkit.dynmap:spigot:3.1")
	compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.2.0-SNAPSHOT")

	implementation("org.litote.kmongo:kmongo:4.4.0")
	implementation("co.aikar:acf-paper:0.5.0-SNAPSHOT")
	implementation("net.kyori:adventure-text-minimessage:4.10.0-SNAPSHOT")

	// TODO: Remove these
	compileOnly("com.discordsrv:discordsrv:1.20.0")
	compileOnly("com.github.bloodmc:GriefDefenderAPI:master")
	compileOnly("com.sk89q.worldguard:worldguard-core:7.0.0-SNAPSHOT")

	implementation("redis.clients:jedis:4.1.0")
	implementation("org.ejml:ejml-all:0.41")
	implementation("com.github.jkcclemens:khttp:0.1.0")
	implementation("io.github.config4k:config4k:0.4.2")
	implementation("net.wesjd:anvilgui:1.5.0-SNAPSHOT")
	implementation("club.minnced:discord-webhooks:0.7.4")
	implementation("ninja.egg82:event-chain-bukkit:1.0.7")
	implementation("com.googlecode.cqengine:cqengine:3.0.0")
	implementation("com.github.stefvanschie.inventoryframework:IF:0.5.8")
	implementation("com.daveanthonythomas.moshipack:moshipack:1.0.1")
}

tasks {
	compileKotlin { kotlinOptions { jvmTarget = "17" } }
	shadowJar { minimize() }
}

java.toolchain.languageVersion.set(of(17))