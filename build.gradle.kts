plugins {
	id("com.github.johnrengelman.shadow") version "7.1.2"
	id("io.papermc.paperweight.userdev") version "1.3.7"
	id("org.jetbrains.kotlin.jvm") version "1.7.0"
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
	paperDevBundle("1.19-R0.1-SNAPSHOT")

	compileOnly("net.luckperms:api:5.4")
	compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
	compileOnly("net.citizensnpcs:citizens:2.0.27-SNAPSHOT")
	compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.2.8")
	compileOnly("com.discordsrv:discordsrv:1.25.1")

	implementation("org.litote.kmongo:kmongo:4.6.0")
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
	shadowJar {
		arrayOf(
			"co.aikar.commands",
			"co.aikar.locales",
			"com.daveanthonythomas.moshipack",
			"com.esotericsoftware.asm",
			"com.esotericsoftware.kryo",
			"com.esotericsoftware.minlog",
			"com.esotericsoftware.reflectasm",
			"com.fasterxml.jackson.annotation",
			"com.fasterxml.jackson.core",
			"com.fasterxml.jackson.databind",
			"com.fasterxml.jackson.module.kotlin",
			"com.github.stefvanschie.inventoryframework",
			"com.googlecode.concurrenttrees.common",
			"com.googlecode.concurrenttrees.radix",
			"com.googlecode.concurrenttrees.radixinverted",
			"com.googlecode.concurrenttrees.radixreversed",
			"com.googlecode.concurrenttrees.solver",
			"com.googlecode.concurrenttrees.suffix",
			"com.googlecode.cqengine",
			"com.mongodb",
			"com.squareup.moshi",
			"com.typesafe.config",
			"de.javakaffee.kryoserializers",
			"de.undercouch.bson4jackson",
			"io.github.config4k",
			"javassist",
			"khttp",
			"net.jodah.typetools",
			"net.wesjd.anvilgui",
			"okio",
			"org.antlr.v4.runtime",
			"org.apache.commons.pool2",
			"org.bson",
			"org.intellij.lang.annotations",
			"org.jetbrains.annotations",
			"org.json",
			"org.litote.jackson",
			"org.litote.kmongo",
			"org.litote.kreflect",
			"org.objenesis",
			"org.slf4j",
			"org.sqlite",
			"redis.clients.jedis"
		).forEach { group ->
			relocate(group, "net.starlegacy.libraries.$group")
		}
	}

	reobfJar {
		outputJar.set(file(rootProject.projectDir.absolutePath + "/build/IonCore.jar"))
	}

	build {
		dependsOn("reobfJar")
	}
}