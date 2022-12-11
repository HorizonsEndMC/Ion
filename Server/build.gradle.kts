plugins {
	id("com.github.johnrengelman.shadow") version "7.1.2"
	id("io.papermc.paperweight.userdev") version "1.3.11"
	java

	id("com.diffplug.spotless")
	kotlin("jvm")
}

repositories {
	mavenCentral()

	maven("https://repo.horizonsend.net/mirror") // Horizon's End Mirror

	maven("https://repo.aikar.co/content/groups/aikar/") // Annotation Command Framework (Paper)
	maven("https://repo.alessiodp.com/releases") // Libby (Required by Citizens)
	maven("https://repo.codemc.io/repository/maven-snapshots/") // WorldEdit
	maven("https://nexus.scarsz.me/content/groups/public/") // DiscordSRV
	maven("https://jitpack.io") // Dynmap (Spigot), Vault, khttp
	maven("https://maven.citizensnpcs.co/repo") // Citizens
}

dependencies {
	implementation(project(":Common"))

	// Platform
	paperDevBundle("1.19.2-R0.1-SNAPSHOT")

	// Plugin Dependencies
	compileOnly("com.github.webbukkit.dynmap:spigot:3.1") { exclude("org.bukkit") /* Old Version */ }
	compileOnly("net.citizensnpcs:citizens-main:2.0.30-SNAPSHOT")
	compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.2.8")
	compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
	compileOnly("com.discordsrv:discordsrv:1.26.0")
	compileOnly("net.luckperms:api:5.4")

	// Included Dependencies
	implementation("com.github.jkcclemens:khttp:0.1.0") { exclude("org.jetbrains.kotlin") /* Old Version */ }
	implementation("com.github.stefvanschie.inventoryframework:IF:0.10.7")
	implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")
	implementation("net.wesjd:anvilgui:1.5.3-SNAPSHOT")

	// Library Loaded Dependencies
	compileOnly("com.daveanthonythomas.moshipack:moshipack:1.0.1")
	compileOnly("com.googlecode.cqengine:cqengine:3.6.0")
	compileOnly("io.github.config4k:config4k:0.5.0")

	// Common Library Loaded Dependencies
	compileOnly("org.spongepowered:configurate-extra-kotlin:4.1.2")
	compileOnly("org.spongepowered:configurate-hocon:4.1.2")

	compileOnly("org.jetbrains.kotlin:kotlin-reflect:1.7.22")
	compileOnly("org.jetbrains.kotlin:kotlin-stdlib:1.7.22")

	compileOnly("org.litote.kmongo:kmongo:4.8.0")
	compileOnly("redis.clients:jedis:4.3.1")
}

tasks.reobfJar { outputJar.set(file(rootProject.projectDir.absolutePath + "/build/IonServer.jar")) }

tasks.build {
	dependsOn("spotlessApply")
	dependsOn("reobfJar")
}