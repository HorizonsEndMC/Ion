package net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.noise

import kotlin.random.Random

interface NoiseWrapper {
	fun setSeed(random: Random) {}
}
