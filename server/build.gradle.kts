plugins {
	id("io.papermc.paperweight.userdev") version "1.5.3"
	kotlin("plugin.serialization")
}

dependencies {
	paperweight.paperDevBundle("1.19.3-R0.1-SNAPSHOT") // Platform

	// Other Plugins
	compileOnly(libs.dynmap) { exclude("org.bukkit") /* Old Version */ }
	compileOnly(libs.citizens)
	compileOnly(libs.worldedit)
	compileOnly(libs.vault)
	compileOnly(libs.discordsrv)
	compileOnly(libs.luckperms)

	// Included
	implementation(libs.acfPaper)
	implementation(libs.anvilgui)
	implementation(libs.config4k)
	implementation(libs.cqengine)
	implementation(libs.inventoryframework)
	implementation(libs.jackson)
	implementation(libs.moshipack)
}

tasks.reobfJar { outputJar.set(file(rootProject.projectDir.absolutePath + "/build/IonServer.jar")) }
tasks.build { dependsOn("reobfJar") }
