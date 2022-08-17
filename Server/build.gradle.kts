plugins {
	id("io.papermc.paperweight.userdev")
	kotlin("jvm")
}

repositories {
	mavenCentral()

	maven("https://repo.papermc.io/repository/maven-public/") // Paper
	maven("https://repo.aikar.co/content/groups/aikar/") // Annotation Command Framework
	maven("https://jitpack.io") // Vault
}

dependencies {
	paperDevBundle("1.19.2-R0.1-SNAPSHOT")

	compileOnly(project(":IonCore"))
	compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")

	implementation(project(":Common"))

	implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")
}