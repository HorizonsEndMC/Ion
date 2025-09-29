package net.horizonsend.ion.server.features.world.environment.weather.type

import net.horizonsend.ion.server.configuration.util.DurationConfig
import net.horizonsend.ion.server.configuration.util.DurationRange
import net.horizonsend.ion.server.core.registration.keys.WeatherTypeKeys
import net.horizonsend.ion.server.features.world.environment.isInside
import net.horizonsend.ion.server.features.world.environment.weather.WeatherManager
import net.kyori.adventure.sound.Sound
import org.bukkit.Particle
import org.bukkit.Registry
import org.bukkit.Sound.ITEM_ELYTRA_FLYING
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.time.Duration
import java.util.concurrent.TimeUnit

object Blizzard : WeatherType(WeatherTypeKeys.BLIZZARD) {
	override fun getDuration(worldWeatherManager: WeatherManager): Duration {
		return DurationRange( //TODO
			DurationConfig(TimeUnit.SECONDS, 1),
			DurationConfig(TimeUnit.SECONDS, 10),
		).getRandomInRange()
	}

	override fun onStart(worldWeatherManager: WeatherManager) {
		soundInterval = 0
	}

	var soundInterval = 0

	override fun tickAsync(worldWeatherManager: WeatherManager) {
		val players = worldWeatherManager.environmentManager.world.world.players

		if (soundInterval >= 10) {
			soundInterval = 0
		}

		soundInterval++

		for (player in players) {
			if (isInside(player.location, 1)) continue
			player.spawnParticle(Particle.SNOWFLAKE, player.location.x, player.location.y, player.location.z, 50, 1.5, 1.5, 1.5, 0.5)

			player.freezeTicks = minOf(player.freezeTicks + 3, 200)

			if (soundInterval == 1) {
				player.playSound(SOUND, player)
			}
		}
	}

	override fun tickSync(worldWeatherManager: WeatherManager) {
		val players = worldWeatherManager.environmentManager.world.world.players

		for (player in players) {
			if (isInside(player.location, 1)) continue
			player.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 30, 1))
		}
	}

	private val SOUND = Sound.sound().type(Registry.SOUNDS.getKeyOrThrow(ITEM_ELYTRA_FLYING).key()).pitch(1f).source(Sound.Source.WEATHER).build()

	override fun getWindSpeedMultiplier(): Double = 3.0
	override fun getWindChangeMultiplier(): Double = 50.0
}
