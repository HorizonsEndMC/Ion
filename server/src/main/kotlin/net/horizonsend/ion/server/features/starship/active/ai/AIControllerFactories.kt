package net.horizonsend.ion.server.features.starship.active.ai

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.starship.active.ai.module.combat.CombatModule
import net.horizonsend.ion.server.features.starship.active.ai.module.combat.FrigateCombatModule
import net.horizonsend.ion.server.features.starship.active.ai.module.combat.StarfighterCombatModule
import net.horizonsend.ion.server.features.starship.active.ai.module.misc.AggroUponDamageModule
import net.horizonsend.ion.server.features.starship.active.ai.module.movement.CruiseModule
import net.horizonsend.ion.server.features.starship.active.ai.module.pathfinding.PathfindingModule
import net.horizonsend.ion.server.features.starship.active.ai.module.pathfinding.SteeringPathfindingModule
import net.horizonsend.ion.server.features.starship.active.ai.module.positioning.AxisStandoffPositioningModule
import net.horizonsend.ion.server.features.starship.active.ai.module.positioning.CirclingPositionModule
import net.horizonsend.ion.server.features.starship.active.ai.module.positioning.PositioningModule
import net.horizonsend.ion.server.features.starship.active.ai.module.positioning.StandoffPositioningModule
import net.horizonsend.ion.server.features.starship.active.ai.module.targeting.ClosestTargetingModule
import net.horizonsend.ion.server.features.starship.active.ai.module.targeting.TargetingModule

object AIControllerFactories : IonServerComponent() {
	val presetControllers = mutableMapOf<String, AIControllerFactory>()

	val starfighter = registerFactory("STARFIGHTER") {
		setControllerTypeName("Starfighter")

		addModule("targeting") { ClosestTargetingModule(it, 5000.0, null).apply { sticky = false } }
		addModule("combat") { StarfighterCombatModule(it, (it.modules["targeting"] as TargetingModule)::findTarget) }
		addModule("aggro") { AggroUponDamageModule(it, (it.modules["targeting"] as StarfighterCombatModule)) }
		addModule("positioning") { AxisStandoffPositioningModule(it, (it.modules["targeting"] as TargetingModule)::findTarget, 25.0) }
		addModule("pathfinding") { SteeringPathfindingModule(it, (it.modules["positioning"] as PositioningModule)::findPositionVec3i) }
		addModule("movement") {
			CruiseModule(
				it,
				it.modules["pathfinding"] as PathfindingModule,
				(it.modules["pathfinding"] as PathfindingModule)::getDestination,
				CruiseModule.ShiftFlightType.ALL,
				256.0
			)
		}

		build()
	}

	val corvette = registerFactory("CORVETTE") {
		setControllerTypeName("Corvette")

		addModule("targeting") { ClosestTargetingModule(it, 5000.0, null).apply { sticky = false } }
		addModule("combat") { StarfighterCombatModule(it, (it.modules["targeting"] as TargetingModule)::findTarget) }
		addModule("aggro") { AggroUponDamageModule(it, (it.modules["targeting"] as CombatModule)) }
		addModule("positioning") { StandoffPositioningModule(it, (it.modules["targeting"] as TargetingModule)::findTarget, 40.0) }
		addModule("pathfinding") { SteeringPathfindingModule(it, (it.modules["positioning"] as PositioningModule)::findPositionVec3i) }
		addModule("movement") {
			CruiseModule(
				it,
				it.modules["pathfinding"] as PathfindingModule,
				(it.modules["pathfinding"] as PathfindingModule)::getDestination,
				CruiseModule.ShiftFlightType.ALL,
				256.0
			)
		}

		build()
	}

	val frigate = registerFactory("FRIGATE") {
		setControllerTypeName("Frigate")

		addModule("targeting") { ClosestTargetingModule(it, 5000.0, null).apply { sticky = false } }
		addModule("combat") { FrigateCombatModule(it, (it.modules["targeting"] as TargetingModule)::findTarget).apply { shouldFaceTarget = false } }
		addModule("aggro") { AggroUponDamageModule(it, (it.modules["targeting"] as CombatModule)) }
		addModule("positioning") { CirclingPositionModule(it, (it.modules["targeting"] as TargetingModule)::findTarget, 40.0) }
		addModule("pathfinding") { SteeringPathfindingModule(it, (it.modules["positioning"] as PositioningModule)::findPositionVec3i) }
		addModule("movement") {
			CruiseModule(
				it,
				it.modules["pathfinding"] as PathfindingModule,
				(it.modules["pathfinding"] as PathfindingModule)::getDestination,
				CruiseModule.ShiftFlightType.ALL,
				256.0
			)
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
