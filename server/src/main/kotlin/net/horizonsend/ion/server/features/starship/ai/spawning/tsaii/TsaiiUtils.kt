package net.horizonsend.ion.server.features.starship.ai.spawning.tsaii

import net.horizonsend.ion.server.configuration.AISpawningConfiguration
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.features.starship.ai.AIControllerFactories
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
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor

val TSAII_LIGHT_ORANGE = TextColor.fromHexString("#F37F58")!!
val TSAII_MEDIUM_ORANGE = TextColor.fromHexString("#E56034")!!
val TSAII_DARK_ORANGE = TextColor.fromHexString("#A1543A")!!
val TSAII_VERY_DARK_ORANGE = TextColor.fromHexString("#9C3614")!!

private val smackPrefix = text("Receiving transmission from Tsaii vessel", TSAII_LIGHT_ORANGE)
private val tsaiiSmackTalk = arrayOf(
	text("I'll leave nothing but scrap"),
	text("I'll cut you to bacon"),
	text("When I'm done with you, I'll mantle your skull!")
)

val tsaiiFrigate: AIControllerFactory = registerFactory("TSAII_FRIGATE") {
	AIControllerFactory.Builder(AIControllerFactories.frigate).build()
}

val tsaiiCorvette: AIControllerFactory = registerFactory("TSAII_CORVETTE") {
	AIControllerFactory.Builder(AIControllerFactories.corvette).build()
}

// Tsaii controllers: aggressive, never running.
val tsaiiStarfighter = registerFactory("TSAII_STARFIGHTER") { // TODO
	setControllerTypeName("Starfighter")
	setModuleBuilder {
		val builder = AIControllerFactory.Builder.ModuleBuilder()

		val targeting = builder.addModule("targeting", ClosestTargetingModule(it, 1500.0, null).apply { sticky = true })
		builder.addModule("combat", StarfighterCombatModule(it, targeting::findTarget))

		val positioning = builder.addModule("positioning", AxisStandoffPositioningModule(it, targeting::findTarget, 25.0))
		val pathfinding = builder.addModule("pathfinding", SteeringPathfindingModule(it, positioning::findPositionVec3i))
		builder.addModule("movement", CruiseModule(it, pathfinding, pathfinding::getDestination, CruiseModule.ShiftFlightType.ALL, 256.0))
		builder.addModule("smackTalk", SmackTalkModule(it, smackPrefix, *tsaiiSmackTalk))

		builder.addModule("warning", RadiusMessageModule(it, mapOf(
			2500.0 to text("Get any closer and you'll be meeting your maker.", TextColor.fromHexString("#FFA500")),
			1500.0 to text("You can't run or hide in space, little ship!", NamedTextColor.RED),
		)))

		builder
	}
	build()
}

// Tsaii controllers: aggressive, never running.
val tsaiiGunship = registerFactory("TSAII_GUNSHIP") { // TODO
	setControllerTypeName("Starfighter")
	setModuleBuilder {
		val builder = AIControllerFactory.Builder.ModuleBuilder()

		val targeting = builder.addModule("targeting", ClosestTargetingModule(it, 1500.0, null).apply { sticky = true })
		builder.addModule("combat", StarfighterCombatModule(it, targeting::findTarget))

		val positioning = builder.addModule("positioning", StandoffPositioningModule(it, targeting::findTarget, 55.0))
		val pathfinding = builder.addModule("pathfinding", SteeringPathfindingModule(it, positioning::findPositionVec3i))
		builder.addModule("movement", CruiseModule(it, pathfinding, pathfinding::getDestination, CruiseModule.ShiftFlightType.ALL, 256.0))
		builder.addModule("smackTalk", SmackTalkModule(it, smackPrefix, *tsaiiSmackTalk))

		builder.addModule("warning", RadiusMessageModule(it, mapOf(1500.0 to text("You can't run or hide in space, little ship!", TextColor.fromHexString("#FFA500")))))

		builder
	}
	build()
}


val bastion = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "BASTION",
	schematicName = "Bastion",
	miniMessageName = "<${TSAII_VERY_DARK_ORANGE.asHexString()}>Bastion",
	type = StarshipType.AI_BATTLECRUISER,
	controllerFactory = "TSAII_FRIGATE",
	xpMultiplier = 0.8,
	creditReward = 8000.0
)

val reaver = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "REAVER",
	schematicName = "Reaver",
	miniMessageName = "<${TSAII_VERY_DARK_ORANGE.asHexString()}>Reaver",
	type = StarshipType.AI_DESTROYER,
	controllerFactory = "AI_FRIGATE",
	xpMultiplier = 0.8,
	creditReward = 8000.0
)

val raider = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "RAIDER",
	schematicName = "Raider",
	miniMessageName = "<${TSAII_VERY_DARK_ORANGE.asHexString()}>Raider",
	type = StarshipType.AI_GUNSHIP,
	controllerFactory = "TSAII_GUNSHIP",
	xpMultiplier = 0.8,
	creditReward = 3000.0
)

val scythe = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "SCYTHE",
	schematicName = "Scythe",
	miniMessageName = "<${TSAII_DARK_ORANGE.asHexString()}>Scythe",
	type = StarshipType.AI_STARFIGHTER,
	controllerFactory = "TSAII_STARFIGHTER",
	xpMultiplier = 0.8,
	creditReward = 1850.0
)

val swarmer = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "SWARMER",
	schematicName = "Swarmer",
	miniMessageName = "<${TSAII_DARK_ORANGE.asHexString()}>Swarmer",
	type = StarshipType.AI_STARFIGHTER,
	controllerFactory = "TSAII_STARFIGHTER",
	xpMultiplier = 0.8,
	creditReward = 1850.0
)

val tsaiiTemplates = arrayOf(
	swarmer,
	scythe,
	raider,
//	reaver,
//	bastion
)
