package net.horizonsend.ion.server.features.ai

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.ai.module.combat.DefensiveCombatModule
import net.horizonsend.ion.server.features.ai.module.combat.FrigateCombatModule
import net.horizonsend.ion.server.features.ai.module.combat.StarfighterCombatModule
import net.horizonsend.ion.server.features.ai.module.misc.FleeModule
import net.horizonsend.ion.server.features.ai.module.movement.CruiseModule
import net.horizonsend.ion.server.features.ai.module.pathfinding.SteeringPathfindingModule
import net.horizonsend.ion.server.features.ai.module.positioning.AxisStandoffPositioningModule
import net.horizonsend.ion.server.features.ai.module.positioning.BasicPositioningModule
import net.horizonsend.ion.server.features.ai.module.positioning.CirclingPositionModule
import net.horizonsend.ion.server.features.ai.module.positioning.StandoffPositioningModule
import net.horizonsend.ion.server.features.ai.module.targeting.ClosestTargetingModule
import net.horizonsend.ion.server.features.ai.module.targeting.HighestDamagerTargetingModule
import net.horizonsend.ion.server.features.ai.module.targeting.TargetingModule
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.distanceToVector
import net.horizonsend.ion.server.miscellaneous.utils.orNull
import java.util.Optional
import kotlin.random.Random

@Suppress("unused") // Entry points
object AIControllerFactories : IonServerComponent() {
	val presetControllers = mutableMapOf<String, AIControllerFactory>()

	val starfighter = registerFactory("STARFIGHTER") {
		setControllerTypeName("Starfighter")
		setModuleBuilder {
			val builder = AIControllerFactory.Builder.ModuleBuilder()

			val targetingOriginal = builder.addModule("targeting", ClosestTargetingModule(it, 500.0, null).apply { sticky = false })
			builder.addModule("combat", StarfighterCombatModule(it) { builder.suppliedModule<TargetingModule>("targeting").get().findTarget() })

			val positioning = builder.addModule("positioning", AxisStandoffPositioningModule(it, { builder.suppliedModule<TargetingModule>("targeting").get().findTarget() }, 25.0))
			val pathfinding = builder.addModule("pathfinding", SteeringPathfindingModule(it, positioning::findPosition))
			val flee = builder.addModule("flee", FleeModule(it, pathfinding::getDestination, targetingOriginal) { controller, _ -> controller.getMinimumShieldHealth() <= 0.2 }) // Flee if a shield reaches below 10%
			builder.addModule("movement", CruiseModule(it, pathfinding, flee, CruiseModule.ShiftFlightType.ALL, 256.0))

			builder
		}
		build()
	}

	val gunship = registerFactory("GUNSHIP") {
		setControllerTypeName("Gunship")
		setModuleBuilder {
			val builder = AIControllerFactory.Builder.ModuleBuilder()

			builder.addModule("targeting", ClosestTargetingModule(it, 500.0, null).apply { sticky = false })
			builder.addModule("combat", StarfighterCombatModule(it) { builder.suppliedModule<TargetingModule>("targeting").get().findTarget() })

			val positioning = builder.addModule("positioning", StandoffPositioningModule(it, { builder.suppliedModule<TargetingModule>("targeting").get().findTarget() }, 55.0))
			val pathfinding = builder.addModule("pathfinding", SteeringPathfindingModule(it, positioning::findPosition))
			builder.addModule("movement", CruiseModule(it, pathfinding, pathfinding::getDestination, CruiseModule.ShiftFlightType.ALL, 256.0))

			builder
		}
		build()
    }

	val gunship_pulse = registerFactory("GUNSHIP_PULSE") {
		setControllerTypeName("PulseGunship")
		setModuleBuilder {
			val builder = AIControllerFactory.Builder.ModuleBuilder()

			builder.addModule("targeting", ClosestTargetingModule(it, 500.0, null).apply { sticky = false })
			builder.addModule("combat", StarfighterCombatModule(it) { builder.suppliedModule<TargetingModule>("targeting").get().findTarget() })

			val positioning = builder.addModule("positioning", StandoffPositioningModule(it, { builder.suppliedModule<TargetingModule>("targeting").get().findTarget() }, 55.0))
			val pathfinding = builder.addModule("pathfinding", SteeringPathfindingModule(it, positioning::findPosition))
			builder.addModule("movement", CruiseModule(it, pathfinding, pathfinding::getDestination, CruiseModule.ShiftFlightType.ALL, 256.0))

			builder
		}
		build()
	}

	val corvette = registerFactory("CORVETTE") {
		setControllerTypeName("Corvette")
		setModuleBuilder {
			val builder = AIControllerFactory.Builder.ModuleBuilder()

			builder.addModule("targeting", ClosestTargetingModule(it, 500.0, null).apply { sticky = false })
			builder.addModule("combat", StarfighterCombatModule(it) { builder.suppliedModule<TargetingModule>("targeting").get().findTarget() })

			val positioning = builder.addModule("positioning", StandoffPositioningModule(it, { builder.suppliedModule<TargetingModule>("targeting").get().findTarget() }, 55.0))
			val pathfinding = builder.addModule("pathfinding", SteeringPathfindingModule(it, positioning::findPosition))
			builder.addModule("movement", CruiseModule(it, pathfinding, pathfinding::getDestination, CruiseModule.ShiftFlightType.ALL, 256.0))

			builder
		}
		build()
	}

	val frigate = registerFactory("FRIGATE") {
        setControllerTypeName("Frigate")

        setModuleBuilder {
			val builder = AIControllerFactory.Builder.ModuleBuilder()

			builder.addModule("targeting", ClosestTargetingModule(it, 1500.0, null).apply { sticky = true })
			builder.addModule("combat", FrigateCombatModule(it) { builder.suppliedModule<TargetingModule>("targeting").get().findTarget() })

			val positioning = builder.addModule("positioning", CirclingPositionModule(it, { builder.suppliedModule<TargetingModule>("targeting").get().findTarget() }, 55.0))
			val pathfinding = builder.addModule("pathfinding", SteeringPathfindingModule(it, positioning::findPosition))
			builder.addModule("movement", CruiseModule(it, pathfinding, pathfinding::getDestination, CruiseModule.ShiftFlightType.ALL, 256.0))

			builder
        }

        build()
    }

	val passive_cruise = registerFactory("EXPLORER_CRUISE") {
		setControllerTypeName("Starfighter")

		val cruiseEndpoint: (AIController) -> Optional<Vec3i> = lambda@{ controller: AIController ->
			var iterations = 0
			val origin = controller.getCenter()

			val world = controller.getWorld()
			val border = world.worldBorder

			val minX = (border.center.x - border.size).toInt()
			val minZ = (border.center.z - border.size).toInt()
			val maxX = (border.center.x + border.size).toInt()
			val maxZ = (border.center.z + border.size).toInt()

			while (iterations < 15) {
				iterations++

				val endPointX = Random.nextInt(minX, maxX)
				val endPointZ = Random.nextInt(minZ, maxZ)
				val endPoint = Vec3i(endPointX, origin.y, endPointZ)

				val planets = Space.getPlanets().filter { it.spaceWorld == world }.map { it.location.toVector() }

				val minDistance = planets.minOfOrNull {
					val direction = endPoint.minus(origin)

					distanceToVector(origin.toVector(), direction.toVector(), it)
				}

				// If there are planets, and the distance to any of them along the path of travel is less than 500, discard
				if (minDistance != null && minDistance <= 500.0) continue

				return@lambda Optional.of(endPoint)
			}

			Optional.empty()
		}

		setModuleBuilder {
			val builder = AIControllerFactory.Builder.ModuleBuilder()

			// Combat handling
			val targeting = builder.addModule("targeting", HighestDamagerTargetingModule(it))
			builder.addModule("combat", DefensiveCombatModule(it, targeting::findTarget))

			// Movement handling
			val positioning = builder.addModule("positioning", BasicPositioningModule(it, cruiseEndpoint.invoke(it).orNull() ?: Vec3i(0, 0, 0)))
			val pathfinding = builder.addModule("pathfinding", SteeringPathfindingModule(it, positioning::findPosition))
			val flee = builder.addModule("flee", FleeModule(it, positioning::getDestination, targeting) { controller, _ -> controller.getMinimumShieldHealth() <= 0.5 }) // Flee if there is a target found by the highest damage module
			builder.addModule("movement", CruiseModule(it, pathfinding, flee, CruiseModule.ShiftFlightType.ALL, 256.0))

			builder
		}

		build()
	}

	operator fun get(identifier: String) = presetControllers[identifier] ?: throw NoSuchElementException("Controller factory $identifier does not exist!")

	fun registerFactory(
		identifier: String,
		factory: AIControllerFactory.Builder.() -> AIControllerFactory
	): AIControllerFactory {
		val built = factory(AIControllerFactory.Builder(identifier))

		presetControllers[identifier] = built
		return built
	}
}
