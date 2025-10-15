package net.horizonsend.ion.server.features.world.environment.weather.configuration

import kotlinx.serialization.Serializable
import net.horizonsend.ion.server.configuration.util.DurationConfig
import net.horizonsend.ion.server.configuration.util.DurationRange
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.features.world.environment.weather.type.WeatherType
import java.util.concurrent.TimeUnit

@Serializable
class WeatherTypeConfiguration(
	@Serializable(with = IonRegistryKey.Companion::class)
	val type: IonRegistryKey<WeatherType, out WeatherType>,
	val duration: DurationRange = DurationRange(DurationConfig(TimeUnit.MINUTES, 5), DurationConfig(TimeUnit.MINUTES, 15))
)
