package net.horizonsend.ion.server.features.starship.active.ai.spawning.privateer

import net.horizonsend.ion.server.configuration.AIShipConfiguration
import net.horizonsend.ion.server.configuration.AIShipConfiguration.AIStarshipTemplate.WeaponSet
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.features.starship.active.ai.AIControllerFactories.registerFactory
import net.horizonsend.ion.server.features.starship.active.ai.AIControllerFactory
import net.horizonsend.ion.server.features.starship.active.ai.module.combat.StarfighterCombatModule
import net.horizonsend.ion.server.features.starship.active.ai.module.misc.AggroUponDamageModule
import net.horizonsend.ion.server.features.starship.active.ai.module.misc.SmackTalkModule
import net.horizonsend.ion.server.features.starship.active.ai.module.movement.CruiseModule
import net.horizonsend.ion.server.features.starship.active.ai.module.pathfinding.SteeringPathfindingModule
import net.horizonsend.ion.server.features.starship.active.ai.module.positioning.AxisStandoffPositioningModule
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.TextColor

object PrivateerUtils {
	val PRIVATEER_LIGHT_TEAL = TextColor.fromHexString("#79B698")
	val PRIVATEER_DARK_TEAL = TextColor.fromHexString("#639f77")

	private val smackTalkList = arrayOf(
		text(""),
		text(""),
		text(""),
		text(""),
		text(""),
		text(""),
		text("")
	)

	// Privateer controllers passive, only becoming aggressive if fired upon
	val privateerStarfighter = registerFactory("PRIVATEER_STARFIGHTER") {
		setControllerTypeName("Starfighter")

		setModuleBuilder {
			val builder = AIControllerFactory.Builder.ModuleBuilder()
			val targeting = builder.addModule("aggro", AggroUponDamageModule(it) { aiController, aggroEngine -> StarfighterCombatModule(aiController, aggroEngine::findTarget) })
			val positioning = builder.addModule("positioning", AxisStandoffPositioningModule(it, targeting::findTarget, 25.0))
			val pathfinding = builder.addModule("pathfinding", SteeringPathfindingModule(it, positioning::findPositionVec3i))
			builder.addModule("movement", CruiseModule(it, pathfinding, pathfinding::getDestination, CruiseModule.ShiftFlightType.ALL, 256.0))
			builder.addModule("smackTalk", SmackTalkModule(it, *smackTalkList))
			builder
		}
		build()
	}

	// Privateer controllers passive, only becoming aggressive if fired upon
	val privateerCorvette = registerFactory("PRIVATEER_CORVETTE") {
		setControllerTypeName("Corvette")

		setModuleBuilder {
			val builder = AIControllerFactory.Builder.ModuleBuilder()
			val targeting = builder.addModule("aggro", AggroUponDamageModule(it) { aiController, aggroEngine -> StarfighterCombatModule(aiController, aggroEngine::findTarget) })
			val positioning = builder.addModule("positioning", AxisStandoffPositioningModule(it, targeting::findTarget, 25.0))
			val pathfinding = builder.addModule("pathfinding", SteeringPathfindingModule(it, positioning::findPositionVec3i))
			builder.addModule("movement", CruiseModule(it, pathfinding, pathfinding::getDestination, CruiseModule.ShiftFlightType.ALL, 256.0))
			builder.addModule("smackTalk", SmackTalkModule(it, *smackTalkList))

			builder
		}
		build()
	}

	val bulwark = AIShipConfiguration.AIStarshipTemplate(
		identifier = "BULWARK",
		schematicName = "Bulwark",
		miniMessageName = "<#63A077>Bulwark",
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

	val contractor = AIShipConfiguration.AIStarshipTemplate(
		identifier = "CONTRACTOR",
		schematicName = "Contractor",
		miniMessageName = "<#63A077>Contractor",
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

	val dagger = AIShipConfiguration.AIStarshipTemplate(
		identifier = "DAGGER",
		schematicName = "Dagger",
		miniMessageName = "<#63A077>Dagger",
		type = StarshipType.AI_STARFIGHTER,
		controllerFactory = "PRIVATEER_STARFIGHTER",
		xpMultiplier = 0.5,
		creditReward = 100.0
	)
}
