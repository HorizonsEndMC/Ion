package net.horizonsend.ion.server.core.registration.registries

import net.horizonsend.ion.server.core.registration.keys.KeyRegistry
import net.horizonsend.ion.server.core.registration.keys.RegistryKeys
import net.horizonsend.ion.server.core.registration.keys.WeatherTypeKeys
import net.horizonsend.ion.server.features.world.environment.weather.type.Blizzard
import net.horizonsend.ion.server.features.world.environment.weather.type.WeatherType

class WeatherTypeRegistry : Registry<WeatherType>(RegistryKeys.WEATHER_TYPE) {
	override fun getKeySet(): KeyRegistry<WeatherType> = WeatherTypeKeys

	override fun boostrap() {
		register(WeatherTypeKeys.BLIZZARD, Blizzard)
	}
}
