package net.horizonsend.ion.server.features.starship.ai.spawning.miningcorp

import net.horizonsend.ion.common.utils.text.HEColorScheme
import net.horizonsend.ion.server.configuration.AIShipConfiguration
import net.horizonsend.ion.server.configuration.ServerConfiguration
import net.horizonsend.ion.server.features.space.generation.SpaceGenerationManager
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.features.starship.ai.spawning.findSpawnLocationNearPlayer
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.component1
import net.horizonsend.ion.server.miscellaneous.utils.component2
import net.horizonsend.ion.server.miscellaneous.utils.component3
import net.horizonsend.ion.server.miscellaneous.utils.component4
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.util.Vector
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

val MINING_CORP_LIGHT_ORANGE = HEColorScheme.HE_LIGHT_ORANGE
val MINING_CORP_DARK_ORANGE = TextColor.fromHexString("#D98507")!!

fun getAsteroidBelts(world: World): Collection<ServerConfiguration.AsteroidConfig.AsteroidFeature> {
	val generator = SpaceGenerationManager.getGenerator(world.minecraft) ?: return listOf()

	return generator.configuration.features
}

fun ServerConfiguration.AsteroidConfig.AsteroidFeature.contains(x: Double, y: Double, z: Double): Boolean {
	return (sqrt((x - origin.x).pow(2) + (z - origin.z).pow(2)) - tubeSize).pow(2) + (y - origin.y).pow(2) < tubeRadius.pow(2)
}

fun ServerConfiguration.AsteroidConfig.AsteroidFeature.bounds(): Pair<Vec3i, Vec3i> {
	val (x, y, z) = Vec3i(origin.x, origin.y, origin.z)

	val radius = (tubeRadius + tubeSize).toInt()

	return Vec3i(x - radius, y, z - radius) to Vec3i(x + radius, y, z + radius)
}

fun ServerConfiguration.AsteroidConfig.AsteroidFeature.randomPosition(): Vector {
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

fun findSpawnPosition(configuration: AIShipConfiguration.AISpawnerConfiguration): Location? {
	val locationNearPlayer = findSpawnLocationNearPlayer(configuration) ?: return null

	val (world, x, y, z) = locationNearPlayer

	val belts = getAsteroidBelts(world)

	if (!belts.any { it.contains(x, y, z) }) return null

	return locationNearPlayer
}

val ostrich = AIShipConfiguration.AIStarshipTemplate(
	identifier = "OSTRICH",
	schematicName = "Ostrich",
	miniMessageName = "<${MINING_CORP_DARK_ORANGE.asHexString()}>Ostrich",
	type = StarshipType.AI_LIGHT_FREIGHTER,
	controllerFactory = "EXPLORER_CRUISE",
	xpMultiplier = 0.5,
	creditReward = 100.0
)

val woodpecker = AIShipConfiguration.AIStarshipTemplate(
	identifier = "WOODPECKER",
	schematicName = "Woodpecker",
	miniMessageName = "<${MINING_CORP_LIGHT_ORANGE.asHexString()}>Woodpecker",
	type = StarshipType.AI_SHUTTLE,
	controllerFactory = "EXPLORER_CRUISE",
	xpMultiplier = 0.5,
	creditReward = 100.0
)

val beaver = AIShipConfiguration.AIStarshipTemplate(
	identifier = "BEAVER",
	schematicName = "Beaver",
	miniMessageName = "<${MINING_CORP_LIGHT_ORANGE.asHexString()}>Beaver",
	type = StarshipType.AI_TRANSPORT,
	controllerFactory = "EXPLORER_CRUISE",
	xpMultiplier = 0.5,
	creditReward = 100.0
)

val badger = AIShipConfiguration.AIStarshipTemplate(
	identifier = "BADGER",
	schematicName = "Badger",
	miniMessageName = "<${MINING_CORP_DARK_ORANGE.asHexString()}>Badger",
	type = StarshipType.AI_LIGHT_FREIGHTER,
	controllerFactory = "EXPLORER_CRUISE",
	xpMultiplier = 0.5,
	creditReward = 100.0
)

val typeV11 = AIShipConfiguration.AIStarshipTemplate(
	identifier = "TYPE_V-11",
	schematicName = "typeV11",
	miniMessageName = "<${MINING_CORP_LIGHT_ORANGE.asHexString()}>Type <${HEColorScheme.HE_LIGHT_GRAY.asHexString()}>V-11",
	type = StarshipType.AI_SHUTTLE,
	controllerFactory = "EXPLORER_CRUISE",
	xpMultiplier = 0.5,
	creditReward = 100.0
)

val typeA21b = AIShipConfiguration.AIStarshipTemplate(
	identifier = "TYPE_A-21B",
	schematicName = "typeA21b",
	miniMessageName = "<${MINING_CORP_LIGHT_ORANGE.asHexString()}>Type <${HEColorScheme.HE_LIGHT_GRAY.asHexString()}>A-21b",
	type = StarshipType.AI_SHUTTLE,
	controllerFactory = "EXPLORER_CRUISE",
	xpMultiplier = 0.5,
	creditReward = 100.0
)

val typeI41 = AIShipConfiguration.AIStarshipTemplate(
	identifier = "TYPE_I-41",
	schematicName = "typeI41",
	miniMessageName = "<${MINING_CORP_LIGHT_ORANGE.asHexString()}>Type <${HEColorScheme.HE_LIGHT_GRAY.asHexString()}>I-41",
	type = StarshipType.AI_SHUTTLE,
	controllerFactory = "EXPLORER_CRUISE",
	xpMultiplier = 0.5,
	creditReward = 100.0
)
