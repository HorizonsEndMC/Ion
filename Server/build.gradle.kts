plugins {
	id("org.jetbrains.kotlin.jvm")
}

repositories {
	mavenCentral()

	maven("https://repo.papermc.io/repository/maven-public/") // Paper
	maven("https://repo.aikar.co/content/groups/aikar/") // Annotation Command Framework
}

dependencies {
	compileOnly("io.papermc.paper:paper-api:1.19-R0.1-SNAPSHOT")

	compileOnly(project(":IonCore"))

	implementation(project(":Common"))

	implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")

	implementation("org.reflections:reflections:0.10.2")
}