package net.horizonsend.ion.server.features.starship.ai.spawning.miningcorp

import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.configuration.AISpawningConfiguration
import net.horizonsend.ion.server.configuration.ServerConfiguration
import net.horizonsend.ion.server.features.space.generation.SpaceGenerationManager
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.features.starship.ai.AIControllerFactories
import net.horizonsend.ion.server.features.starship.ai.AIControllerFactory
import net.horizonsend.ion.server.features.starship.ai.module.combat.StarfighterCombatModule
import net.horizonsend.ion.server.features.starship.ai.module.movement.CruiseModule
import net.horizonsend.ion.server.features.starship.ai.module.pathfinding.SteeringPathfindingModule
import net.horizonsend.ion.server.features.starship.ai.module.positioning.AxisStandoffPositioningModule
import net.horizonsend.ion.server.features.starship.ai.module.targeting.ClosestTargetingModule
import net.horizonsend.ion.server.features.starship.ai.module.targeting.TargetingModule
import net.horizonsend.ion.server.features.starship.ai.spawning.privateer.PRIVATEER_LIGHT_TEAL
import net.horizonsend.ion.server.features.starship.ai.spawning.privateer.PRIVATEER_MEDIUM_TEAL
import net.horizonsend.ion.server.features.starship.ai.spawning.privateer.bulwark
import net.horizonsend.ion.server.features.starship.ai.spawning.privateer.contractor
import net.horizonsend.ion.server.features.starship.ai.spawning.privateer.dagger
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.TextColor
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
val miningGuildMini = "<$MINING_CORP_LIGHT_ORANGE>Mining <$MINING_CORP_DARK_ORANGE>Guild"

val reinforcementMiniMessage = "$miningGuildMini <$HE_MEDIUM_GRAY>vessel {5} in distress, requesting immediate reinforcements."

// Privateer controllers passive, only becoming aggressive if fired upon
@Suppress("unused")
val miningCorpStarfighter = AIControllerFactories.registerFactory("MINING_CORP_STARFIGHTER") {
	setControllerTypeName("Starfighter")
	setModuleBuilder {
		val builder = AIControllerFactory.Builder.ModuleBuilder()

		builder.addModule("targeting", ClosestTargetingModule(it, 500.0, null).apply { sticky = false })
		builder.addModule("combat", StarfighterCombatModule(it) { builder.suppliedModule<TargetingModule>("targeting").get().findTarget() })

		val positioning = builder.addModule("positioning", AxisStandoffPositioningModule(it, { builder.suppliedModule<TargetingModule>("targeting").get().findTarget() }, 45.0))
		val pathfinding = builder.addModule("pathfinding", SteeringPathfindingModule(it, positioning::findPosition))
		builder.addModule("movement", CruiseModule(it, pathfinding, pathfinding::getDestination, CruiseModule.ShiftFlightType.ALL, 256.0))

		builder
	}

	build()
}

private val MINING_CORP_SMACK_PREFIX: String = "<$HE_MEDIUM_GRAY>Receiving transmission from $miningGuildMini <$HE_MEDIUM_GRAY>vessel"

private fun basicMiningCorpTemplate(
	identifier: String,
	schematicName: String,
	miniMessageName: String,
	type: StarshipType,
	controllerFactory: String,
	creditReward: Double,
	xpMultiplier: Double,
	engagementRadius: Double = 550.0,
	manualWeaponSets: MutableSet<AISpawningConfiguration.AIStarshipTemplate.WeaponSet> = mutableSetOf(),
	autoWeaponSets: MutableSet<AISpawningConfiguration.AIStarshipTemplate.WeaponSet> = mutableSetOf(),
	reinforcementThreshold: Double,
	reinforcementShips: Map<String, Int>,
): AISpawningConfiguration.AIStarshipTemplate {
	val reinforcementConfig = AISpawningConfiguration.AISpawnerConfiguration(
		miniMessageSpawnMessage = "${miningGuildMini}<$HE_MEDIUM_GRAY> backup request acknowledged. {0} responding at {1}, {3}, in {4}",
		pointChance = 0.0,
		pointThreshold = Int.MAX_VALUE,
		minDistanceFromPlayer = 100.0,
		maxDistanceFromPlayer = 150.0,
		tiers = listOf(
			AISpawningConfiguration.AISpawnerTier(
				identifier = "REINFORCEMENTS",
				nameList = mapOf(
					"<$PRIVATEER_MEDIUM_TEAL>System Defense <$PRIVATEER_LIGHT_TEAL>First Responder" to 5
				),
				ships = reinforcementShips
			)
		)
	)

	return AISpawningConfiguration.AIStarshipTemplate(
		color = MINING_CORP_LIGHT_ORANGE.value(),
		smackInformation = null,
		radiusMessageInformation = AISpawningConfiguration.AIStarshipTemplate.RadiusMessageInformation(
			prefix = MINING_CORP_SMACK_PREFIX,
			messages = mapOf(
				engagementRadius * 1.5 to "<#FFA500>You are entering restricted airspace. If you hear this transmission, turn away immediately or you will be fired upon.",
				engagementRadius to "<RED>You have violated restricted airspace. Your vessel will be fired upon."
			)
		),
		maxSpeed = -1,
		reinforcementInformation = AISpawningConfiguration.AIStarshipTemplate.ReinforcementInformation(
			activationThreshold = reinforcementThreshold,
			delay = 100L,
			broadcastMessage = reinforcementMiniMessage,
			configuration = reinforcementConfig
		),

		engagementRange = engagementRadius,
		identifier = identifier,
		schematicName = schematicName,
		miniMessageName = miniMessageName,
		type = type,
		controllerFactory = controllerFactory,
		xpMultiplier = xpMultiplier,
		creditReward = creditReward,
		manualWeaponSets = manualWeaponSets,
		autoWeaponSets = autoWeaponSets
	)
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

val ostrich = basicMiningCorpTemplate(
	identifier = "OSTRICH",
	schematicName = "Ostrich",
	miniMessageName = "<$MINING_CORP_DARK_ORANGE>Ostrich",
	type = StarshipType.AI_LIGHT_FREIGHTER,
	controllerFactory = "MINING_CORP_STARFIGHTER",
	xpMultiplier = 0.6,
	creditReward = 2650.0,
	reinforcementThreshold = 0.75,
	reinforcementShips = mapOf(bulwark.identifier to 2)
)

val woodpecker = basicMiningCorpTemplate(
	identifier = "WOODPECKER",
	schematicName = "Woodpecker",
	miniMessageName = "<${MINING_CORP_LIGHT_ORANGE.asHexString()}>Woodpecker",
	type = StarshipType.AI_SHUTTLE,
	controllerFactory = "MINING_CORP_STARFIGHTER",
	xpMultiplier = 0.6,
	creditReward = 650.0,
	reinforcementThreshold = 0.65,
	reinforcementShips = mapOf(dagger.identifier to 2)
)

val beaver = basicMiningCorpTemplate(
	identifier = "BEAVER",
	schematicName = "Beaver",
	miniMessageName = "<${MINING_CORP_LIGHT_ORANGE.asHexString()}>Beaver",
	type = StarshipType.AI_TRANSPORT,
	controllerFactory = "MINING_CORP_STARFIGHTER",
	xpMultiplier = 0.6,
	creditReward = 1850.0,
	reinforcementThreshold = 0.5,
	reinforcementShips = mapOf(
		contractor.identifier to 10,
		bulwark.identifier to 5,
	)
)

val badger = basicMiningCorpTemplate(
	identifier = "BADGER",
	schematicName = "Badger",
	miniMessageName = "<${MINING_CORP_DARK_ORANGE.asHexString()}>Badger",
	type = StarshipType.AI_LIGHT_FREIGHTER,
	controllerFactory = "MINING_CORP_STARFIGHTER",
	xpMultiplier = 0.6,
	creditReward = 2650.0,
	reinforcementThreshold = 0.75,
	reinforcementShips = mapOf(
		bulwark.identifier to 2
	)
)

val typeV11 = basicMiningCorpTemplate(
	identifier = "TYPE_V-11",
	schematicName = "typeV11",
	miniMessageName = "<${MINING_CORP_LIGHT_ORANGE.asHexString()}>Type <${HEColorScheme.HE_LIGHT_GRAY.asHexString()}>V-11",
	type = StarshipType.AI_SHUTTLE,
	controllerFactory = "MINING_CORP_STARFIGHTER",
	xpMultiplier = 0.6,
	creditReward = 650.0,
	reinforcementThreshold = 0.55,
	reinforcementShips = mapOf(dagger.identifier to 2)
)

val typeA21b = basicMiningCorpTemplate(
	identifier = "TYPE_A-21B",
	schematicName = "typeA21b",
	miniMessageName = "<${MINING_CORP_LIGHT_ORANGE.asHexString()}>Type <${HEColorScheme.HE_LIGHT_GRAY.asHexString()}>A-21b",
	type = StarshipType.AI_SHUTTLE,
	controllerFactory = "MINING_CORP_STARFIGHTER",
	xpMultiplier = 0.6,
	creditReward = 650.0,
	reinforcementThreshold = 0.55,
	reinforcementShips = mapOf(dagger.identifier to 2)
)

val typeI41 = basicMiningCorpTemplate(
	identifier = "TYPE_I-41",
	schematicName = "typeI41",
	miniMessageName = "<${MINING_CORP_LIGHT_ORANGE.asHexString()}>Type <${HEColorScheme.HE_LIGHT_GRAY.asHexString()}>I-41",
	type = StarshipType.AI_SHUTTLE,
	controllerFactory = "MINING_CORP_STARFIGHTER",
	xpMultiplier = 0.6,
	creditReward = 650.0,
	reinforcementThreshold = 0.65,
	reinforcementShips = mapOf(dagger.identifier to 2)
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
