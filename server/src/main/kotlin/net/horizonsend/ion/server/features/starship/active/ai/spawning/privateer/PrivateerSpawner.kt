package net.horizonsend.ion.server.features.starship.active.ai.spawning.privateer

import net.horizonsend.ion.server.configuration.AIShipConfiguration
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.starship.active.ai.spawning.AISpawner
import net.horizonsend.ion.server.features.starship.active.ai.spawning.getLocationNear
import net.horizonsend.ion.server.features.starship.active.ai.spawning.getNonProtectedPlayer
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Location
import java.util.function.Supplier

abstract class PrivateerSpawner(
	identifier: String,
	configurationSupplier: Supplier<AIShipConfiguration.AISpawnerConfiguration>
) : AISpawner(identifier, configurationSupplier) {
	protected fun findLocation(): Location? {
		// Get a random world based on the weight in the config
		val worldConfig = configuration.worldWeightedRandomList.random()
		val world = worldConfig.getWorld()

		val player = getNonProtectedPlayer(world) ?: return null

		var iterations = 0

		val border = world.worldBorder

		val planets = Space.getPlanets().filter { it.spaceWorld == world }.map { it.location.toVector() }

		// max 10 iterations
		while (iterations <= 15) {
			iterations++

			val loc = player.getLocationNear(minDistanceFromPlayer, maxDistanceFromPlayer)

			if (!border.isInside(loc)) continue

			if (planets.any { it.distanceSquared(loc.toVector()) <= 250000 }) continue

			return loc
		}

		return null
	}

	companion object PrivateerColorScheme {
		val PRIVATEER_LIGHT_TEAL = TextColor.fromHexString("#79B698")
		val PRIVATEER_DARK_TEAL = TextColor.fromHexString("#639f77")
	}
}
