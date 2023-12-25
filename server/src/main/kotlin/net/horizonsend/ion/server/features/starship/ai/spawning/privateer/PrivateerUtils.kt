package net.horizonsend.ion.server.features.starship.ai.spawning.privateer

import net.horizonsend.ion.server.configuration.AISpawningConfiguration
import net.horizonsend.ion.server.configuration.AISpawningConfiguration.AIStarshipTemplate.WeaponSet
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.features.starship.ai.AIControllerFactories.registerFactory
import net.horizonsend.ion.server.features.starship.ai.AIControllerFactory
import net.horizonsend.ion.server.features.starship.ai.module.combat.StarfighterCombatModule
import net.horizonsend.ion.server.features.starship.ai.module.misc.RadiusMessageModule
import net.horizonsend.ion.server.features.starship.ai.module.misc.SmackTalkModule
import net.horizonsend.ion.server.features.starship.ai.module.movement.CruiseModule
import net.horizonsend.ion.server.features.starship.ai.module.pathfinding.SteeringPathfindingModule
import net.horizonsend.ion.server.features.starship.ai.module.positioning.AxisStandoffPositioningModule
import net.horizonsend.ion.server.features.starship.ai.module.positioning.StandoffPositioningModule
import net.horizonsend.ion.server.features.starship.ai.module.targeting.ClosestTargetingModule
import net.horizonsend.ion.server.features.starship.ai.spawning.findSpawnLocationNearPlayer
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Location

val PRIVATEER_LIGHTER_TEAL = TextColor.fromHexString("#48e596")!!
val PRIVATEER_LIGHT_TEAL = TextColor.fromHexString("#5DD097")!!
val PRIVATEER_MEDIUM_TEAL = TextColor.fromHexString("#79B698")!!
val PRIVATEER_DARK_TEAL = TextColor.fromHexString("#639f77")!!

private val smackTalkList = arrayOf(
	text("Message 1"),
	text("Message 2"),
	text("Message 3"),
	text("Message 4"),
	text("Message 5"),
	text("Message 6"),
	text("Message 7")
)

val smackPrefix = text("Receiving transmission from privateer vessel: ", PRIVATEER_LIGHTER_TEAL)

// Privateer controllers passive, only becoming aggressive if fired upon
val privateerStarfighter = registerFactory("PRIVATEER_STARFIGHTER") {
	setControllerTypeName("Starfighter")
	setModuleBuilder {
		val builder = AIControllerFactory.Builder.ModuleBuilder()

		val targeting = builder.addModule("targeting", ClosestTargetingModule(it, 500.0, null).apply { sticky = false })
		builder.addModule("combat", StarfighterCombatModule(it, targeting::findTarget))

		val positioning = builder.addModule("positioning", AxisStandoffPositioningModule(it, targeting::findTarget, 25.0))
		val pathfinding = builder.addModule("pathfinding", SteeringPathfindingModule(it, positioning::findPositionVec3i))
		builder.addModule("movement", CruiseModule(it, pathfinding, pathfinding::getDestination, CruiseModule.ShiftFlightType.ALL, 256.0))
		builder.addModule("smackTalk", SmackTalkModule(it, smackPrefix, *smackTalkList))

		builder.addModule("warning", RadiusMessageModule(it, mapOf(
			1000.0 to text("You are entering restricted airspace. If you hear this transmission, turn away immediately or you will be fired upon.", TextColor.fromHexString("#FFA500")),
			500.0 to text("You have violated restricted airspace. Your vessel will be fired upon.", RED)
		)))

		builder
	}
	build()
}

// Privateer controllers passive, only becoming aggressive if fired upon
val privateerGunship = registerFactory("PRIVATEER_GUNSHIP") {
	setControllerTypeName("Starfighter")
	setModuleBuilder {
		val builder = AIControllerFactory.Builder.ModuleBuilder()

		val targeting = builder.addModule("targeting", ClosestTargetingModule(it, 500.0, null).apply { sticky = false })
		builder.addModule("combat", StarfighterCombatModule(it, targeting::findTarget))

		val positioning = builder.addModule("positioning", StandoffPositioningModule(it, targeting::findTarget, 55.0))
		val pathfinding = builder.addModule("pathfinding", SteeringPathfindingModule(it, positioning::findPositionVec3i))
		builder.addModule("movement", CruiseModule(it, pathfinding, pathfinding::getDestination, CruiseModule.ShiftFlightType.ALL, 256.0))
		builder.addModule("smackTalk", SmackTalkModule(it, smackPrefix, *smackTalkList))

		builder.addModule("warning", RadiusMessageModule(it, mapOf(
			1000.0 to text("You are entering restricted airspace. If you hear this transmission, turn away immediately or you will be fired upon.", TextColor.fromHexString("#FFA500")),
			500.0 to text("You have violated restricted airspace. Your vessel will be fired upon.", RED)
		)))

		builder
	}
	build()
}

// Privateer controllers passive, only becoming aggressive if fired upon
val privateerCorvette = registerFactory("PRIVATEER_CORVETTE") {
	setControllerTypeName("Corvette")
	setModuleBuilder {
		val builder = AIControllerFactory.Builder.ModuleBuilder()

		val targeting = builder.addModule("targeting", ClosestTargetingModule(it, 500.0, null).apply { sticky = false })
		builder.addModule("combat", StarfighterCombatModule(it, targeting::findTarget))

		val positioning = builder.addModule("positioning", StandoffPositioningModule(it, targeting::findTarget, 55.0))
		val pathfinding = builder.addModule("pathfinding", SteeringPathfindingModule(it, positioning::findPositionVec3i))
		builder.addModule("movement", CruiseModule(it, pathfinding, pathfinding::getDestination, CruiseModule.ShiftFlightType.ALL, 256.0))
		builder.addModule("smackTalk", SmackTalkModule(it, smackPrefix, *smackTalkList))

		builder.addModule("warning", RadiusMessageModule(it, mapOf(
			1000.0 to text("You are entering restricted airspace. If you hear this transmission, turn away immediately or you will be fired upon.", TextColor.fromHexString("#FFA500")),
			500.0 to text("You have violated restricted airspace. Your vessel will be fired upon.", RED)
		)))

		builder
	}
	build()
}

val bulwark = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "BULWARK",
	schematicName = "Bulwark",
	miniMessageName = "<${PRIVATEER_DARK_TEAL.asHexString()}>Bulwark",
	type = StarshipType.AI_CORVETTE,
	controllerFactory = "PRIVATEER_CORVETTE",
	xpMultiplier = 0.5,
	creditReward = 100.0,
	manualWeaponSets = mutableSetOf(
		WeaponSet(name = "main", engagementRangeMin = 0.0, engagementRangeMax = 350.0)
	),
	autoWeaponSets = mutableSetOf(
		WeaponSet(name = "lt1", engagementRangeMin = 0.0, engagementRangeMax = 250.0),
		WeaponSet(name = "tt1", engagementRangeMin = 250.0, engagementRangeMax = 550.0)
	)
)

val contractor = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "CONTRACTOR",
	schematicName = "Contractor",
	miniMessageName = "<${PRIVATEER_MEDIUM_TEAL.asHexString()}>Contractor",
	type = StarshipType.AI_GUNSHIP,
	controllerFactory = "PRIVATEER_GUNSHIP",
	xpMultiplier = 0.5,
	creditReward = 100.0,
	manualWeaponSets = mutableSetOf(
		WeaponSet(name = "manual", engagementRangeMin = 0.0, engagementRangeMax = 500.0)
	),
	autoWeaponSets = mutableSetOf(
		WeaponSet(name = "auto", engagementRangeMin = 0.0, engagementRangeMax = 500.0)
	)
)

val dagger = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "DAGGER",
	schematicName = "Dagger",
	miniMessageName = "<${PRIVATEER_LIGHT_TEAL.asHexString()}>Dagger",
	type = StarshipType.AI_STARFIGHTER,
	controllerFactory = "PRIVATEER_STARFIGHTER",
	xpMultiplier = 0.5,
	creditReward = 100.0
)

val daybreak = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "DAYBREAK",
	schematicName = "Daybreak",
	miniMessageName = "<${PRIVATEER_LIGHT_TEAL.asHexString()}>Daybreak",
	type = StarshipType.AI_CORVETTE,
	controllerFactory = "PRIVATEER_CORVETTE",
	xpMultiplier = 0.5,
	creditReward = 100.0
)

val patroller = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "PATROLLER",
	schematicName = "Patroller",
	miniMessageName = "<${PRIVATEER_LIGHT_TEAL.asHexString()}>Patroller",
	type = StarshipType.AI_GUNSHIP,
	controllerFactory = "PRIVATEER_GUNSHIP",
	xpMultiplier = 0.5,
	creditReward = 100.0
)

val protector = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "PROTECTOR",
	schematicName = "Protector",
	miniMessageName = "<${PRIVATEER_LIGHT_TEAL.asHexString()}>Protector",
	type = StarshipType.AI_SHUTTLE, // TODO go back to SF
	controllerFactory = "PRIVATEER_STARFIGHTER",
	xpMultiplier = 0.5,
	creditReward = 100.0
)

val veteran = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "VETERAN",
	schematicName = "Veteran",
	miniMessageName = "<${PRIVATEER_LIGHT_TEAL.asHexString()}>Veteran",
	type = StarshipType.AI_GUNSHIP,
	controllerFactory = "PRIVATEER_GUNSHIP",
	xpMultiplier = 0.5,
	creditReward = 100.0
)

val teneta = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "TENETA",
	schematicName = "Teneta",
	miniMessageName = "<${PRIVATEER_LIGHT_TEAL.asHexString()}>Teneta",
	type = StarshipType.AI_SHUTTLE, // TODO go back to SF
	controllerFactory = "PRIVATEER_STARFIGHTER",
	xpMultiplier = 0.5,
	creditReward = 100.0
)

val furious = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "FURIOUS",
	schematicName = "Furious",
	miniMessageName = "<${PRIVATEER_LIGHT_TEAL.asHexString()}>Furious",
	type = StarshipType.AI_SHUTTLE, // TODO go back to SF
	controllerFactory = "PRIVATEER_STARFIGHTER",
	xpMultiplier = 0.5,
	creditReward = 100.0
)

val inflict = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "INFLICT",
	schematicName = "Inflict",
	miniMessageName = "<${PRIVATEER_LIGHT_TEAL.asHexString()}>Inflict",
	type = StarshipType.AI_SHUTTLE, // TODO go back to SF
	controllerFactory = "PRIVATEER_STARFIGHTER",
	xpMultiplier = 0.5,
	creditReward = 100.0
)

fun findPrivateerSpawnLocation(configuration: AISpawningConfiguration.AISpawnerConfiguration): Location?  {
	val nearPlayer = findSpawnLocationNearPlayer(configuration) ?: return null

//	val world: World = nearPlayer.world

	return nearPlayer
}
