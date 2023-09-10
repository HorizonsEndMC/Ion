import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension

val kotlinVersion = "1.8.21"
val joobyVersion = "3.0.5"

plugins {
	val joobyVersion = "3.0.5"

	id("application")
	id("io.jooby.run") version joobyVersion
	id("io.spring.dependency-management") version "1.1.0"
	id("com.google.osdetector") version "1.7.3"
	id("com.github.johnrengelman.shadow") version "8.1.1"

	kotlin("jvm")
	kotlin("kapt")
}

group = "app"
version = "1.0.0"

application {
	mainClass.set("com.rattly.quizwar.AppKt")
}

repositories {
	mavenLocal()
	mavenCentral()
}

configure<DependencyManagementExtension> {
	imports {
		mavenBom("io.jooby:jooby-bom:$joobyVersion")
	}
}

dependencies {
	implementation(project(":common"))

	implementation("io.jooby:jooby-undertow")
	implementation("io.jooby:jooby-kotlin")
	implementation("io.jooby:jooby-apt")
	implementation("io.jooby:jooby-whoops")
	implementation("io.jooby:jooby-pebble")
	implementation("io.jooby:jooby-jackson")
	implementation("io.jooby:jooby-openapi")
	implementation("io.jooby:jooby-swagger-ui")

	implementation("ch.qos.logback:logback-core:1.4.11")
	implementation("ch.qos.logback:logback-classic:1.4.11")
	implementation("org.reflections:reflections:0.10.2")
	implementation("io.swagger.core.v3:swagger-annotations:+")
	implementation("com.github.vladimir-bukhtoyarov:bucket4j-core:7.6.0")

	kotlin("kotlin-stdlib-jdk8")
	kapt("io.jooby:jooby-apt")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
	kotlinOptions.javaParameters = true
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
	mergeServiceFiles()
}

kapt {
	arguments {
		arg("jooby.incremental", true)
		arg("jooby.services", true)
		arg("jooby.debug", false)
	}
}

