package net.horizonsend.ion.server.features.starship.ai

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.starship.ai.module.combat.FrigateCombatModule
import net.horizonsend.ion.server.features.starship.ai.module.combat.StarfighterCombatModule
import net.horizonsend.ion.server.features.starship.ai.module.movement.CruiseModule
import net.horizonsend.ion.server.features.starship.ai.module.pathfinding.SteeringPathfindingModule
import net.horizonsend.ion.server.features.starship.ai.module.positioning.AxisStandoffPositioningModule
import net.horizonsend.ion.server.features.starship.ai.module.positioning.CirclingPositionModule
import net.horizonsend.ion.server.features.starship.ai.module.positioning.StandoffPositioningModule
import net.horizonsend.ion.server.features.starship.ai.module.targeting.ClosestTargetingModule

@Suppress("unused") // Entry points
object AIControllerFactories : IonServerComponent() {
	val presetControllers = mutableMapOf<String, AIControllerFactory>()

	val starfighter = registerFactory("STARFIGHTER") {
        setControllerTypeName("Starfighter")

        setModuleBuilder {
            val builder = AIControllerFactory.Builder.ModuleBuilder()

            val targeting = builder.addModule("targeting", ClosestTargetingModule(it, 5000.0, null).apply { sticky = false })
            builder.addModule("combat", StarfighterCombatModule(it, targeting::findTarget))
            val positioning = builder.addModule("positioning", AxisStandoffPositioningModule(it, targeting::findTarget, 25.0))
            val pathfinding = builder.addModule("pathfinding", SteeringPathfindingModule(it, positioning::findPositionVec3i))
            builder.addModule(
                "movement",
                CruiseModule(
                    it,
                    pathfinding,
                    pathfinding::getDestination,
                    CruiseModule.ShiftFlightType.ALL,
                    256.0
                )
            )

            builder
        }

        build()
    }

	val gunship = registerFactory("GUNSHIP") {
        setControllerTypeName("Gunship")

        setModuleBuilder {
            val builder = AIControllerFactory.Builder.ModuleBuilder()

            val targeting = builder.addModule("targeting", ClosestTargetingModule(it, 5000.0, null).apply { sticky = false })
            builder.addModule("combat", StarfighterCombatModule(it, targeting::findTarget))
            val positioning = builder.addModule("positioning", StandoffPositioningModule(it, targeting::findTarget, 40.0))
            val pathfinding = builder.addModule("pathfinding", SteeringPathfindingModule(it, positioning::findPositionVec3i))
            builder.addModule(
                "movement",
                CruiseModule(
                    it,
                    pathfinding,
                    pathfinding::getDestination,
                    CruiseModule.ShiftFlightType.ALL,
                    256.0
                )
            )

            builder
        }

        build()
    }

	val corvette = registerFactory("CORVETTE") {
        setControllerTypeName("Corvette")

        setModuleBuilder {
            val builder = AIControllerFactory.Builder.ModuleBuilder()

            val targeting = builder.addModule("targeting", ClosestTargetingModule(it, 5000.0, null).apply { sticky = false })
            builder.addModule("combat", StarfighterCombatModule(it, targeting::findTarget))
            val positioning = builder.addModule("positioning", StandoffPositioningModule(it, targeting::findTarget, 40.0))
            val pathfinding = builder.addModule("pathfinding", SteeringPathfindingModule(it, positioning::findPositionVec3i))
            builder.addModule(
                "movement",
                CruiseModule(
                    it,
                    pathfinding,
                    pathfinding::getDestination,
                    CruiseModule.ShiftFlightType.ALL,
                    256.0
                )
            )

            builder
        }

        build()
    }

	val frigate = registerFactory("FRIGATE") {
        setControllerTypeName("Frigate")

        setModuleBuilder {
            val builder = AIControllerFactory.Builder.ModuleBuilder()

            val targeting = builder.addModule("targeting", ClosestTargetingModule(it, 5000.0, null).apply { sticky = false })
            builder.addModule("combat", FrigateCombatModule(it, targeting::findTarget).apply { shouldFaceTarget = false })
            val positioning = builder.addModule("positioning", CirclingPositionModule(it, targeting::findTarget, 40.0))
            val pathfinding = builder.addModule("pathfinding", SteeringPathfindingModule(it, positioning::findPositionVec3i))
            builder.addModule(
                "movement",
                CruiseModule(
                    it,
                    pathfinding,
                    pathfinding::getDestination,
                    CruiseModule.ShiftFlightType.ALL,
                    256.0
                )
            )

            builder
        }

        build()
    }

	operator fun get(identifier: String) = presetControllers[identifier]!!

	fun registerFactory(
		identifier: String,
		factory: AIControllerFactory.Builder.() -> AIControllerFactory
	): AIControllerFactory {
		val built = factory(AIControllerFactory.Builder())

		presetControllers[identifier] = built
		return built
	}
}
