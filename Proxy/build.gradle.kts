plugins {
	id("org.jetbrains.kotlin.jvm")
}

repositories {
	mavenCentral()

	maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
	compileOnly("com.velocitypowered:velocity-api:3.1.2-SNAPSHOT")

	implementation(project(":Common"))
}

