pluginManagement {
	repositories {
		gradlePluginPortal()

		maven("https://repo.papermc.io/repository/maven-public/") // Paperweight Userdev
	}
}

rootProject.name = "Ion"

include("IonCore")
include("Proxy")
include("Server")
include("Common")