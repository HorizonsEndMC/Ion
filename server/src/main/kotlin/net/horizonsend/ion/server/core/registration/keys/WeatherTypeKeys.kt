package net.horizonsend.ion.server.core.registration.keys

import net.horizonsend.ion.server.core.registration.keys.RegistryKeys.WEATHER_TYPE
import net.horizonsend.ion.server.features.world.environment.weather.type.WeatherType

object WeatherTypeKeys : KeyRegistry<WeatherType>(WEATHER_TYPE, WeatherType::class) {
	val BLIZZARD = registerKey("BLIZZARD")
}
