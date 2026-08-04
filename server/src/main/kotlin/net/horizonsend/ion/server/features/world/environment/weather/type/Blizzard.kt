package net.horizonsend.ion.server.features.world.environment.weather.type

import net.horizonsend.ion.server.configuration.util.DurationConfig
import net.horizonsend.ion.server.configuration.util.DurationRange
import net.horizonsend.ion.server.core.registration.keys.WeatherTypeKeys
import net.horizonsend.ion.server.features.world.environment.isInside
import net.horizonsend.ion.server.features.world.environment.tickEnvironmentModule
import net.horizonsend.ion.server.features.world.environment.weather.WeatherManager
import net.kyori.adventure.sound.Sound
import org.bukkit.Color
import org.bukkit.GameMode
import org.bukkit.Particle
import org.bukkit.Particle.Trail
import org.bukkit.Registry
import org.bukkit.Sound.ITEM_ELYTRA_FLYING
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.time.Duration
import java.util.concurrent.TimeUnit
import kotlin.math.PI
import kotlin.random.Random

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

		if (soundInterval >= 100) {
			soundInterval = 0
		}

		soundInterval++

		for (player in players) {
			if (isInside(player.location, 1)) continue

			spawnParticlesAround(worldWeatherManager, player)
//			player.spawnParticle(Particle.SNOWFLAKE, player.location.x, player.location.y, player.location.z, 5, 1.5, 1.5, 1.5, 0.5)

			if (soundInterval == 1) {
				player.playSound(SOUND, player)
			}
		}
	}

	private const val COUNT = 20

	fun spawnParticlesAround(worldWeatherManager: WeatherManager, player: Player) {
		val eyeLocation = player.eyeLocation
		val windDirection = worldWeatherManager.getWindVector(player.world, eyeLocation.x, eyeLocation.y, eyeLocation.z)
		val radius = 2.5

		val vector = windDirection.clone().normalize().multiply(radius * 0.75).multiply(-1)
		val forwardCenter = eyeLocation.clone().add(vector.clone().multiply(Random.nextDouble(0.25, 2.25)))
		val rearCenter = eyeLocation.clone().subtract(vector.clone())
		val orthogonal = windDirection.clone().normalize().getCrossProduct(BlockFace.UP.direction).multiply(radius * 0.5)

		repeat(COUNT) {
			val radians = Random.nextDouble(0.0, 2.0) * PI
			val around = orthogonal.clone().rotateAroundAxis(vector, radians)

			val particleOrigin = forwardCenter.add(around)
			val particleDestination = rearCenter.add(around.clone().multiply(1.5))

			player.spawnParticle(
				Particle.TRAIL,
				particleOrigin,
				2,
				Trail(particleDestination, Color.WHITE, 10)
			)
		}
	}

	override fun tickSync(worldWeatherManager: WeatherManager) {
		val players = worldWeatherManager.environmentManager.world.world.players

		for (player in players) {
			if (player.gameMode != GameMode.SURVIVAL || player.isDead) return

			if (isInside(player.location, 1)) continue

			if (!tickEnvironmentModule(player, 50)) {
				player.freezeTicks = minOf(player.freezeTicks + 4, 200)
			} else {
				player.freezeTicks = minOf(player.freezeTicks + 2, 200)
			}

			player.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 30, 0))
		}
	}

	private val SOUND = Sound.sound().type(Registry.SOUNDS.getKeyOrThrow(ITEM_ELYTRA_FLYING).key()).pitch(1f).source(Sound.Source.WEATHER).build()

	override fun getWindSpeedMultiplier(): Double = 3.0
	override fun getWindChangeMultiplier(): Double = 10.0
}
