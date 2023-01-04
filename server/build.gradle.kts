@Suppress("DSL_SCOPE_VIOLATION") // TODO: https://youtrack.jetbrains.com/issue/KTIJ-19369
plugins {
	alias(libs.plugins.paperweight)
}

dependencies {
	paperDevBundle(libs.versions.paper.get()) // Platform

	// Other Plugins
	compileOnly(libs.dynmap) { exclude("org.bukkit") /* Old Version */ }
	compileOnly(libs.citizens)
	compileOnly(libs.worldedit)
	compileOnly(libs.vault)
	compileOnly(libs.discordsrv)
	compileOnly(libs.luckperms)

	// Included
	implementation(libs.moshipack) { exclude("kotlin") /* Old Version */ }
	implementation(libs.inventoryframework)
	implementation(libs.acfPaper)
	implementation(libs.anvilgui)

	// Library Loaded
	compileOnly(libs.jackson)
	compileOnly(libs.cqengine)
	compileOnly(libs.config4k)
}

tasks.reobfJar { outputJar.set(file(rootProject.projectDir.absolutePath + "/build/IonServer.jar")) }
tasks.build { dependsOn("reobfJar") }
