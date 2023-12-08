package net.horizonsend.ion.server.features.starship.active.ai.spawning.miningcorp

import net.horizonsend.ion.common.utils.text.HEColorScheme
import net.horizonsend.ion.server.configuration.AIShipConfiguration
import net.horizonsend.ion.server.configuration.ServerConfiguration.AsteroidConfig.AsteroidFeature
import net.horizonsend.ion.server.features.space.generation.SpaceGenerationManager
import net.horizonsend.ion.server.features.starship.active.ai.spawning.AISpawner
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.kyori.adventure.text.format.TextColor
import org.bukkit.World
import org.bukkit.util.Vector
import java.util.concurrent.ThreadLocalRandom
import java.util.function.Supplier
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

abstract class StandardTransportOperation(
	config: Supplier<AIShipConfiguration.AISpawnerConfiguration>,
	name: String,
	pointChance: Double,
	pointThreshold: Int
) : AISpawner("MINING_CORP_$name", config) {
	protected fun getAsteroidBelts(world: World): Collection<AsteroidFeature> {
		val generator = SpaceGenerationManager.getGenerator(world.minecraft) ?: return listOf()

		return generator.configuration.features
	}

	protected fun AsteroidFeature.contains(x: Double, y: Double, z: Double): Boolean {
		return (sqrt((x - origin.x).pow(2) + (z - origin.z).pow(2)) - tubeSize).pow(2) + (y - origin.y).pow(2) < tubeRadius.pow(2)
	}

	protected fun AsteroidFeature.bounds(): Pair<Vec3i, Vec3i> {
		val (x, y , z) = Vec3i(origin.x, origin.y, origin.z)

		val radius = (tubeRadius + tubeSize).toInt()

		return Vec3i(x - radius, y, z - radius) to Vec3i(x + radius, y, z + radius)
	}

	protected fun AsteroidFeature.randomPosition(): Vector {
		val radians = ThreadLocalRandom.current().nextDouble(0.0, 2 * PI)
		val min = tubeSize - tubeRadius
		val max = tubeSize + tubeRadius

		val distance = ThreadLocalRandom.current().nextDouble(min, max)

		return Vector(
			cos(radians) + distance,
			origin.y.toDouble(),
			sin(radians) + distance,
		)
	}

	companion object MiningCorpColorScheme {
		val MINING_CORP_LIGHT_ORANGE = HEColorScheme.HE_LIGHT_ORANGE
		val MINING_CORP_DARK_ORANGE = TextColor.fromHexString("#D98507")
	}
}
