package net.horizonsend.ion.server.features.gas.collectionfactors

import org.bukkit.Location
import kotlin.random.Random

class RandomByHeightFactor(
	private val minAtmosphereHeight: Double,
	private val minChance: Double,
	private val maxAtmosphereHeight: Double,
	private val maxChance: Double
) : CollectionFactor() {
	override fun factor(location: Location): Boolean {
		if (!canBeFound(location)) return false // Quickly check if out of collection area

		val slope = (maxChance - minChance) / (maxAtmosphereHeight-minAtmosphereHeight)

		val ramp = (location.y - minAtmosphereHeight) * slope

		return Random.nextDouble() >= ramp
	}

	override fun canBeFound(location: Location): Boolean {
		return !SpaceOnlyFactor().factor(location) && location.y in minAtmosphereHeight..maxAtmosphereHeight
	}
}
