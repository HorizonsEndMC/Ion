plugins {
	id("io.papermc.paperweight.userdev")
	kotlin("jvm")
	java
}

dependencies {
	paperDevBundle("1.19.2-R0.1-SNAPSHOT")

	compileOnly("net.citizensnpcs:citizens-main:2.0.30-SNAPSHOT")
	compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.2.8")
	compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
	compileOnly("com.discordsrv:discordsrv:1.26.0")
	compileOnly("net.luckperms:api:5.4")

	compileOnly("com.github.webbukkit.dynmap:spigot:3.1") {
		exclude("org.bukkit") // Old Version
	}

	implementation(project(":Common"))

	implementation("com.github.stefvanschie.inventoryframework:IF:0.5.8")
	implementation("com.daveanthonythomas.moshipack:moshipack:1.0.1")
	implementation("com.googlecode.cqengine:cqengine:3.6.0")
	implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")
	implementation("io.github.config4k:config4k:0.5.0")
	implementation("net.wesjd:anvilgui:1.5.3-SNAPSHOT")
	implementation("org.litote.kmongo:kmongo:4.7.1")
	implementation("redis.clients:jedis:3.7.1")

	implementation("com.github.jkcclemens:khttp:0.1.0") {
		exclude("org.jetbrains.kotlin") // Old Version
	}
}