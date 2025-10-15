package net.horizonsend.ion.server.features.world.environment.weather.type

import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.core.registration.Keyed
import net.horizonsend.ion.server.features.world.environment.weather.WeatherManager
import java.time.Duration

abstract class WeatherType(override val key: IonRegistryKey<WeatherType, out WeatherType>) : Keyed<WeatherType> {
	open fun tickSync(worldWeatherManager: WeatherManager) {}
	open fun tickAsync(worldWeatherManager: WeatherManager) {}

	open fun onStart(worldWeatherManager: WeatherManager) {}
	open fun onEnd(worldWeatherManager: WeatherManager) {}

	abstract fun getDuration(worldWeatherManager: WeatherManager): Duration

	abstract fun getWindSpeedMultiplier(): Double
	abstract fun getWindChangeMultiplier(): Double
}
