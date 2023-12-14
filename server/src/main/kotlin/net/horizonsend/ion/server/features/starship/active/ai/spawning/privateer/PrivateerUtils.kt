package net.horizonsend.ion.server.features.starship.active.ai.spawning.privateer

import net.horizonsend.ion.server.configuration.AIShipConfiguration
import net.horizonsend.ion.server.configuration.AIShipConfiguration.AIStarshipTemplate.WeaponSet
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.features.starship.active.ai.AIControllerFactories.registerFactory
import net.horizonsend.ion.server.features.starship.active.ai.AIControllerFactory
import net.horizonsend.ion.server.features.starship.active.ai.module.combat.StarfighterCombatModule
import net.horizonsend.ion.server.features.starship.active.ai.module.misc.AggroUponDamageModule
import net.horizonsend.ion.server.features.starship.active.ai.module.misc.SmackTalkModule
import net.horizonsend.ion.server.features.starship.active.ai.module.movement.CruiseModule
import net.horizonsend.ion.server.features.starship.active.ai.module.pathfinding.SteeringPathfindingModule
import net.horizonsend.ion.server.features.starship.active.ai.module.positioning.AxisStandoffPositioningModule
import net.horizonsend.ion.server.features.starship.active.ai.module.positioning.StandoffPositioningModule
import net.horizonsend.ion.server.features.starship.active.ai.spawning.getLocationNear
import net.horizonsend.ion.server.features.starship.active.ai.spawning.getNonProtectedPlayer
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Location

object PrivateerUtils {
	val PRIVATEER_LIGHT_TEAL = TextColor.fromHexString("#5DD097")!!
	val PRIVATEER_MEDIUM_TEAL = TextColor.fromHexString("#79B698")!!
	val PRIVATEER_DARK_TEAL = TextColor.fromHexString("#639f77")!!

	private val smackTalkList = arrayOf(
		text(""),
		text(""),
		text(""),
		text(""),
		text(""),
		text(""),
		text("")
	)

	val smackPrefix = text("Receiving transmission from privateer vessel: ", PRIVATEER_LIGHT_TEAL)

	// Privateer controllers passive, only becoming aggressive if fired upon
	val privateerStarfighter = registerFactory("PRIVATEER_STARFIGHTER") {
		setControllerTypeName("Starfighter")

		setModuleBuilder {
			val builder = AIControllerFactory.Builder.ModuleBuilder()
			val targeting = builder.addModule("aggro", AggroUponDamageModule(it) { aiController, aggroEngine -> StarfighterCombatModule(aiController, aggroEngine::findTarget) })
			val positioning = builder.addModule("positioning", AxisStandoffPositioningModule(it, targeting::findTarget, 25.0))
			val pathfinding = builder.addModule("pathfinding", SteeringPathfindingModule(it, positioning::findPositionVec3i))
			builder.addModule("movement", CruiseModule(it, pathfinding, pathfinding::getDestination, CruiseModule.ShiftFlightType.ALL, 256.0))
			builder.addModule("smackTalk", SmackTalkModule(it, smackPrefix, *smackTalkList))
			builder
		}
		build()
	}

	// Privateer controllers passive, only becoming aggressive if fired upon
	val privateerGunship = registerFactory("PRIVATEER_STARFIGHTER") {
		setControllerTypeName("Starfighter")

		setModuleBuilder {
			val builder = AIControllerFactory.Builder.ModuleBuilder()
			val targeting = builder.addModule("aggro", AggroUponDamageModule(it) { aiController, aggroEngine -> StarfighterCombatModule(aiController, aggroEngine::findTarget) })
			val positioning = builder.addModule("positioning", StandoffPositioningModule(it, targeting::findTarget, 55.0))
			val pathfinding = builder.addModule("pathfinding", SteeringPathfindingModule(it, positioning::findPositionVec3i))
			builder.addModule("movement", CruiseModule(it, pathfinding, pathfinding::getDestination, CruiseModule.ShiftFlightType.ALL, 256.0))
			builder.addModule("smackTalk", SmackTalkModule(it, smackPrefix, *smackTalkList))
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
			val positioning = builder.addModule("positioning", StandoffPositioningModule(it, targeting::findTarget, 55.0))
			val pathfinding = builder.addModule("pathfinding", SteeringPathfindingModule(it, positioning::findPositionVec3i))
			builder.addModule("movement", CruiseModule(it, pathfinding, pathfinding::getDestination, CruiseModule.ShiftFlightType.ALL, 256.0))
			builder.addModule("smackTalk", SmackTalkModule(it, smackPrefix, *smackTalkList))

			builder
		}
		build()
	}

	val bulwark = AIShipConfiguration.AIStarshipTemplate(
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

	val contractor = AIShipConfiguration.AIStarshipTemplate(
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

	val dagger = AIShipConfiguration.AIStarshipTemplate(
		identifier = "DAGGER",
		schematicName = "Dagger",
		miniMessageName = "<${PRIVATEER_LIGHT_TEAL.asHexString()}>Dagger",
		type = StarshipType.AI_STARFIGHTER,
		controllerFactory = "PRIVATEER_STARFIGHTER",
		xpMultiplier = 0.5,
		creditReward = 100.0
	)

	fun findLocation(): Location?  {
		// Get a random world based on the weight in the config
		val worldConfig = PrivateerPatrolSpawner.configuration.worldWeightedRandomList.random()
		val world = worldConfig.getWorld()

		val player = getNonProtectedPlayer(world) ?: return null

		var iterations = 0

		val border = world.worldBorder

		val planets = Space.getPlanets().filter { it.spaceWorld == world }.map { it.location.toVector() }

		// max 10 iterations
		while (iterations <= 15) {
			iterations++

			val loc = player.getLocationNear(PrivateerPatrolSpawner.minDistanceFromPlayer, PrivateerPatrolSpawner.maxDistanceFromPlayer)

			if (!border.isInside(loc)) continue

			if (planets.any { it.distanceSquared(loc.toVector()) <= 250000 }) continue

			return loc
		}

		return null
	}
}
