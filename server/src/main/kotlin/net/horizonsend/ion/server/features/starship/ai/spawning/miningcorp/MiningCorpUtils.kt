package net.horizonsend.ion.server.features.starship.ai.spawning.miningcorp

import net.horizonsend.ion.common.utils.text.HEColorScheme
import net.horizonsend.ion.common.utils.text.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.configuration.AISpawningConfiguration
import net.horizonsend.ion.server.configuration.ServerConfiguration
import net.horizonsend.ion.server.features.space.generation.SpaceGenerationManager
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.features.starship.ai.AIControllerFactories
import net.horizonsend.ion.server.features.starship.ai.AIControllerFactory
import net.horizonsend.ion.server.features.starship.ai.module.combat.StarfighterCombatModule
import net.horizonsend.ion.server.features.starship.ai.module.misc.RadiusMessageModule
import net.horizonsend.ion.server.features.starship.ai.module.misc.ReinforcementSpawnerModule
import net.horizonsend.ion.server.features.starship.ai.module.movement.CruiseModule
import net.horizonsend.ion.server.features.starship.ai.module.pathfinding.SteeringPathfindingModule
import net.horizonsend.ion.server.features.starship.ai.module.positioning.AxisStandoffPositioningModule
import net.horizonsend.ion.server.features.starship.ai.module.targeting.ClosestTargetingModule
import net.horizonsend.ion.server.features.starship.ai.spawning.findSpawnLocationNearPlayer
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
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

val miningGuild = ofChildren(text("Mining ", MINING_CORP_LIGHT_ORANGE), text("Guild", MINING_CORP_DARK_ORANGE))

val reinforcementMessage = ofChildren(
	miningGuild,
	text(" vessel {5} in distress, requesting immediate reinforcements.", HE_MEDIUM_GRAY),
)

// Privateer controllers passive, only becoming aggressive if fired upon
val miningCorpStarfighter = AIControllerFactories.registerFactory("MINING_CORP_STARFIGHTER") {
	setControllerTypeName("Starfighter")
	setModuleBuilder {
		val builder = AIControllerFactory.Builder.ModuleBuilder()

		val targeting = builder.addModule("targeting", ClosestTargetingModule(it, 500.0, null).apply { sticky = false })
		builder.addModule("combat", StarfighterCombatModule(it, targeting::findTarget))

		val positioning = builder.addModule("positioning", AxisStandoffPositioningModule(it, targeting::findTarget, 25.0))
		val pathfinding = builder.addModule("pathfinding", SteeringPathfindingModule(it, positioning::findPositionVec3i))
		builder.addModule("movement", CruiseModule(it, pathfinding, pathfinding::getDestination, CruiseModule.ShiftFlightType.ALL, 256.0))

		builder.addModule("reinforcement", ReinforcementSpawnerModule(it, MiningCorpReinforcementSpawner(it), 0.5, reinforcementMessage))

		builder.addModule(
			"warning", RadiusMessageModule(
				it, mapOf(
					1000.0 to text(
						"You are entering restricted airspace. If you hear this transmission, turn away immediately or you will be fired upon.",
						TextColor.fromHexString("#FFA500")
					),
					500.0 to text("You have violated restricted airspace. Your vessel will be fired upon.", NamedTextColor.RED)
				)
			)
		)

		builder
	}

	build()
}

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

fun findMiningCorpSpawnPosition(configuration: AISpawningConfiguration.AISpawnerConfiguration): Location? {
	val locationNearPlayer = findSpawnLocationNearPlayer(configuration) ?: return null

	return locationNearPlayer
}

val ostrich = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "OSTRICH",
//	schematicName = "Ostrich",
	miniMessageName = "<$MINING_CORP_DARK_ORANGE>Ostrich",
//	type = StarshipType.AI_LIGHT_FREIGHTER,
	controllerFactory = "MINING_CORP_STARFIGHTER",
	xpMultiplier = 0.5,
	creditReward = 100.0
)

val woodpecker = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "WOODPECKER",
//	schematicName = "Woodpecker",
	miniMessageName = "<${MINING_CORP_LIGHT_ORANGE.asHexString()}>Woodpecker",
	type = StarshipType.AI_SHUTTLE,
	controllerFactory = "MINING_CORP_STARFIGHTER",
	xpMultiplier = 0.5,
	creditReward = 100.0
)

val beaver = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "BEAVER",
//	schematicName = "Beaver",
	miniMessageName = "<${MINING_CORP_LIGHT_ORANGE.asHexString()}>Beaver",
//	type = StarshipType.AI_TRANSPORT,
	controllerFactory = "MINING_CORP_STARFIGHTER",
	xpMultiplier = 0.5,
	creditReward = 100.0
)

val badger = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "BADGER",
//	schematicName = "Badger",
	miniMessageName = "<${MINING_CORP_DARK_ORANGE.asHexString()}>Badger",
//	type = StarshipType.AI_LIGHT_FREIGHTER,
	controllerFactory = "MINING_CORP_STARFIGHTER",
	xpMultiplier = 0.5,
	creditReward = 100.0
)

val typeV11 = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "TYPE_V-11",
//	schematicName = "typeV11",
	miniMessageName = "<${MINING_CORP_LIGHT_ORANGE.asHexString()}>Type <${HEColorScheme.HE_LIGHT_GRAY.asHexString()}>V-11",
	type = StarshipType.AI_SHUTTLE,
	controllerFactory = "MINING_CORP_STARFIGHTER",
	xpMultiplier = 0.5,
	creditReward = 100.0
)

val typeA21b = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "TYPE_A-21B",
//	schematicName = "typeA21b",
	miniMessageName = "<${MINING_CORP_LIGHT_ORANGE.asHexString()}>Type <${HEColorScheme.HE_LIGHT_GRAY.asHexString()}>A-21b",
	type = StarshipType.AI_SHUTTLE,
	controllerFactory = "MINING_CORP_STARFIGHTER",
	xpMultiplier = 0.5,
	creditReward = 100.0
)

val typeI41 = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "TYPE_I-41",
//	schematicName = "typeI41",
	miniMessageName = "<${MINING_CORP_LIGHT_ORANGE.asHexString()}>Type <${HEColorScheme.HE_LIGHT_GRAY.asHexString()}>I-41",
	type = StarshipType.AI_SHUTTLE,
	controllerFactory = "MINING_CORP_STARFIGHTER",
	xpMultiplier = 0.5,
	creditReward = 100.0
)

val miningGuildTemplates = arrayOf(
	ostrich,
	woodpecker,
	beaver,
	badger,
	typeV11,
	typeA21b,
	typeI41
)
