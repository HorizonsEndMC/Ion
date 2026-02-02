package net.horizonsend.ion.server.features.world.environment

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.features.world.IonWorld
import net.horizonsend.ion.server.features.world.environment.modules.EnvironmentModule
import net.horizonsend.ion.server.features.world.environment.weather.WeatherManager

class WorldEnvironmentManager(val world: IonWorld) {
	val weatherManager = WeatherManager(this)
	var modules = mutableSetOf<EnvironmentModule>(); private set

	init {
	    reloadConfiguration()
	}

	var configuration = world.configuration.environments

	fun reloadConfiguration() {
		configuration = world.configuration.environments
		// Deregister all listeners and such in case the module configuration changes
		modules.forEach(EnvironmentModule::onUnload)
		weatherManager.shutdown()

		modules = configuration.moduleConfiguration.mapTo(ObjectOpenHashSet()) { it.buildModule(this) }
		// Reregister all listeners and stuff
		modules.forEach(EnvironmentModule::handleLoad)
		weatherManager.startup()
	}

	fun tickSync() {
		modules.forEach(EnvironmentModule::tickSync)
		weatherManager.tickSync()
	}

	fun tickAsync() {
		modules.forEach(EnvironmentModule::tickAsync)
		weatherManager.tickAsync()
	}
}
