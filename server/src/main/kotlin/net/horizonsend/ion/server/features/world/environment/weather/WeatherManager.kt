package net.horizonsend.ion.server.features.world.environment.weather

import net.horizonsend.ion.common.utils.miscellaneous.getDurationBreakdownString
import net.horizonsend.ion.server.features.world.environment.WorldEnvironmentManager
import net.horizonsend.ion.server.features.world.environment.weather.configuration.WeatherTypeConfiguration
import net.horizonsend.ion.server.features.world.environment.weather.type.WeatherType
import org.bukkit.World
import org.bukkit.util.Vector
import org.bukkit.util.noise.PerlinOctaveGenerator
import java.time.Duration
import java.time.Instant

class WeatherManager(val environmentManager: WorldEnvironmentManager) {
	val configuration get() = environmentManager.configuration.weatherConfiguration

	private var currentWeatherState: WeatherState = Disabled()

	fun shutdown() {
		currentWeatherState = Disabled()
	}

	fun startup() {
		rollWeather()
	}

	fun tickSync() {
		(currentWeatherState as? Active)?.weatherType?.tickSync(this)
	}

	fun tickAsync() {
		rollWeather()
		(currentWeatherState as? Active)?.weatherType?.tickAsync(this)
	}

	fun rollWeather() {
		val time = Instant.now()

		when (currentWeatherState) {
			is Disabled -> {
				val available = getAvailableWeatherTypes()
				if (available.isEmpty()) return

				val next = available.random()
				//TODO maybe a warmup time per weather?
				val separationMillis = configuration.weatherSeparation.getRandomInRange().toMillis()
				val startTime = Instant.ofEpochMilli(time.toEpochMilli() + separationMillis)

				currentWeatherState = Warmup(startTime, next)
			}
			is Warmup -> {
				val warmup = currentWeatherState as Warmup
				if (time < warmup.startTime) return

				currentWeatherState = Active(warmup.weatherType.type.getValue(), warmup.weatherType.duration.getRandomInRange())
				warmup.weatherType.type.getValue().onStart(this)
			}
			is Active -> {
				val active = currentWeatherState as Active
				val timeRemaining = (active.startTime + active.duration.toMillis()) - time.toEpochMilli()
				if (timeRemaining <= 0) {
					active.weatherType.onEnd(this)
					currentWeatherState = Disabled()
				}
			}
		}
	}

	fun getAvailableWeatherTypes(): List<WeatherTypeConfiguration> {
		return configuration.weatherTypes
	}

	abstract inner class WeatherState() {
		abstract fun isActive(): Boolean
	}

	inner class Disabled : WeatherState() {
		override fun isActive(): Boolean = false
	}

	inner class Warmup(val startTime: Instant, val weatherType: WeatherTypeConfiguration) : WeatherState() {
		override fun isActive(): Boolean = false
	}

	inner class Active(val weatherType: WeatherType, val duration: Duration) : WeatherState() {
		val startTime = System.currentTimeMillis()
		override fun isActive(): Boolean = true

		override fun toString(): String {
			return getDurationBreakdownString((startTime + duration.toMillis()) - System.currentTimeMillis())
		}
	}

	fun getWeatherState() = currentWeatherState

	val backgroundWindNoise get() = PerlinOctaveGenerator(environmentManager.world.world, 3)

	fun getWindVector(world: World, x: Double, y: Double, z: Double): Vector {
		val gameTime = world.gameTime.toDouble()

		val weatherState = getWeatherState()

		val windScale = 0.005 * if (weatherState is Active) weatherState.weatherType.getWindChangeMultiplier() else 1.0
		val windSpeed = 2.0 * if (weatherState is Active) weatherState.weatherType.getWindSpeedMultiplier() else 1.0

		val xDir = backgroundWindNoise.noise(x, gameTime, windScale, windSpeed)
		val yDir = backgroundWindNoise.noise(y, gameTime, windScale, windSpeed)
		val zDir = backgroundWindNoise.noise(z, gameTime, windScale, windSpeed)

		return Vector(xDir, yDir, zDir)
	}
}
