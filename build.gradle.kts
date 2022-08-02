plugins {
	id("com.github.johnrengelman.shadow") version "7.1.2"
	id("io.papermc.paperweight.userdev") version "1.3.8"
	id("org.jetbrains.kotlin.jvm") version "1.7.10"
	java
}

repositories {
	mavenCentral()

	maven("https://repo.papermc.io/repository/maven-public/")
	maven("https://repo.aikar.co/content/groups/aikar/") // Annotation Command Framework (Paper), WorldEdit API
	maven("https://jitpack.io") // khttp, VaultAPI, Dynmap (Spigot)
	maven("https://repo.codemc.io/repository/maven-snapshots/") // AnvilGUI
	maven("https://nexus.scarsz.me/content/groups/public/") // DiscordSRV
	maven("https://m2.dv8tion.net/releases") // JDA (Required by DiscordSRV)
	maven("https://repo.citizensnpcs.co/") // Citizens
}

dependencies {
	paperDevBundle("1.19.1-R0.1-SNAPSHOT")

	compileOnly("net.luckperms:api:5.4")
	compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
	compileOnly("net.citizensnpcs:citizens:2.0.27-SNAPSHOT")
	compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.2.8")
	compileOnly("com.discordsrv:discordsrv:1.25.1")

	implementation("org.litote.kmongo:kmongo:4.6.1")
	implementation("io.github.config4k:config4k:0.4.2")
	implementation("com.googlecode.cqengine:cqengine:3.6.0")
	implementation("com.daveanthonythomas.moshipack:moshipack:1.0.1")
	implementation("redis.clients:jedis:3.7.1")
	implementation("com.github.stefvanschie.inventoryframework:IF:0.5.8")
	implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")
	implementation("net.wesjd:anvilgui:1.5.3-SNAPSHOT")
	implementation("com.github.jkcclemens:khttp:0.1.0") {
		exclude("org.jetbrains.kotlin") // Old Version
	}
	compileOnly("com.github.webbukkit.dynmap:spigot:3.1") {
		exclude("org.bukkit") // Old Version
	}
}

tasks {
	reobfJar {
		outputJar.set(file(rootProject.projectDir.absolutePath + "/build/IonCore.jar"))
	}

	build {
		dependsOn("reobfJar")
	}

	compileKotlin {
		kotlinOptions {
			jvmTarget = "17"
		}
	}

	compileJava {
		sourceCompatibility = "17"
		targetCompatibility = "17"
	}
}