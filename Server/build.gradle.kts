plugins {
	id("org.jetbrains.kotlin.jvm")
}

repositories {
	mavenCentral()

	maven("https://repo.papermc.io/repository/maven-public/") // Paper
}

dependencies {
	compileOnly("io.papermc.paper:paper-api:1.19-R0.1-SNAPSHOT")

	compileOnly(project(":IonCore"))

	implementation(project(":Common"))

	// Cloud Command Framework
	implementation("cloud.commandframework:cloud-paper:1.7.0")
}