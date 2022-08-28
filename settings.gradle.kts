pluginManagement {
	repositories {
		gradlePluginPortal()

		maven("https://repo.papermc.io/repository/maven-public/")
	}
}

rootProject.name = "Ion"

include("IonCore")
include("Proxy")
include("Server")
include("Common")