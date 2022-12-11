rootProject.name = "Ion"

include("Proxy")
include("Server")
include("Common")

pluginManagement.repositories {
	maven("https://repo.papermc.io/repository/maven-public/")
	gradlePluginPortal()
}