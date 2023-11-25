package net.horizonsend.ion.server.features.starship.active.ai

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.configuration.AIShipConfiguration
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ai.engine.combat.FrigateCombatEngine
import net.horizonsend.ion.server.features.starship.active.ai.engine.combat.StarfighterCombatEngine
import net.horizonsend.ion.server.features.starship.active.ai.engine.misc.AggroUponDamageEngine
import net.horizonsend.ion.server.features.starship.active.ai.engine.misc.TemporaryControllerEngine
import net.horizonsend.ion.server.features.starship.active.ai.engine.movement.CruiseEngine
import net.horizonsend.ion.server.features.starship.active.ai.engine.pathfinding.SteeringPathfindingEngine
import net.horizonsend.ion.server.features.starship.active.ai.engine.positioning.AxisStandoffPositioningEngine
import net.horizonsend.ion.server.features.starship.active.ai.engine.positioning.CirclingPositionEngine
import net.horizonsend.ion.server.features.starship.active.ai.engine.positioning.StandoffPositioningEngine
import net.horizonsend.ion.server.features.starship.active.ai.engine.targeting.ClosestTargetingEngine
import net.horizonsend.ion.server.features.starship.active.ai.util.AITarget
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.damager.AIShipDamager
import net.kyori.adventure.text.Component
import org.bukkit.Location

object AIControllerFactories : IonServerComponent() {
	val presetControllers = mutableMapOf<String, AIControllerFactory>()

	val STARFIGHTER = registerFactory(
		"STARFIGHTER",
		object : AIControllerFactory("STARFIGHTER") {
			override fun createController(
				starship: ActiveStarship,
				pilotName: Component,
				target: AITarget?,
				destination: Location?,
				manualWeaponSets: Set<AIShipConfiguration.AIStarshipTemplate.WeaponSet>,
				autoWeaponSets: Set<AIShipConfiguration.AIStarshipTemplate.WeaponSet>,
				previousController: AIController?
			): AIController {
				return AIController(
					starship,
					"STARFIGHTER",
					AIShipDamager(starship),
					pilotName,
					manualWeaponSets,
					autoWeaponSets
				).apply {
					val targeting = ClosestTargetingEngine(this, 5000.0, target).apply { sticky = false }

					engines["targeting"] = targeting
					engines["combat"] = StarfighterCombatEngine(this, targeting::findTarget)

					val positioning = AxisStandoffPositioningEngine(this, targeting::findTarget, 25.0)
					engines["positioning"] = positioning

//					val pathfinding = CombatAStarPathfindingEngine(this, positioning::findPositionVec3i)
					val pathfinding = SteeringPathfindingEngine(this, positioning::findPositionVec3i)
					engines["pathfinding"] = pathfinding
					engines["movement"] = CruiseEngine(
						this,
						pathfinding,
						pathfinding::getDestination,
						CruiseEngine.ShiftFlightType.ALL,
						256.0
					)
				}
			}
		}
	)

	val TEMPORARY_STARFIGHTER = registerFactory(
		"TEMPORARY_STARFIGHTER",
		object : AIControllerFactory("TEMPORARY_STARFIGHTER") {
			override fun createController(
				starship: ActiveStarship,
				pilotName: Component,
				target: AITarget?,
				destination: Location?,
				manualWeaponSets: Set<AIShipConfiguration.AIStarshipTemplate.WeaponSet>,
				autoWeaponSets: Set<AIShipConfiguration.AIStarshipTemplate.WeaponSet>,
				previousController: AIController?
			): AIController {
				return AIController(
					starship,
					"TEMPORARY_STARFIGHTER",
					AIShipDamager(starship),
					pilotName,
					manualWeaponSets,
					autoWeaponSets
				).apply {
					val targeting = ClosestTargetingEngine(this, 5000.0, target).apply { sticky = false }

					engines["targeting"] = targeting
					engines["combat"] = StarfighterCombatEngine(this, targeting::findTarget).apply { shouldFaceTarget = true }

					val positioning = AxisStandoffPositioningEngine(this, targeting::findTarget, 25.0)
					engines["positioning"] = positioning

//					val pathfinding = CombatAStarPathfindingEngine(this, positioning::findPositionVec3i)
					val pathfinding = SteeringPathfindingEngine(this, positioning::findPositionVec3i)
					engines["pathfinding"] = pathfinding
					engines["movement"] = CruiseEngine(
						this,
						pathfinding,
						pathfinding::getDestination,
						CruiseEngine.ShiftFlightType.ALL,
						256.0
					)

					engines["fallback"] = TemporaryControllerEngine(this, previousController!!)
				}
			}
		}
	)

//	val CRUISE_STARFIGHTER_FALLBACK = registerFactory(
//		"STARFIGHTER",
//		object : AIControllerFactory("STARFIGHTER") {
//			override fun createController(
//				starship: ActiveStarship,
//				pilotName: Component,
//				target: AITarget?,
//				destination: Location?,
//				aggressivenessLevel: AggressivenessLevel,
//				manualWeaponSets: Set<AIShipConfiguration.AIStarshipTemplate.WeaponSet>,
//				autoWeaponSets: Set<AIShipConfiguration.AIStarshipTemplate.WeaponSet>,
//				previousController: AIController?
//			): AutoCruiseAIController {
//				return AutoCruiseAIController(
//					starship,
//					destination!!,
//					-1,
//					aggressivenessLevel,
//					pilotName,
//					STARFIGHTER
//				)
//			}
//		}
//	)

	val CORVETTE = registerFactory(
		"CORVETTE",
		object : AIControllerFactory("CORVETTE") {
			override fun createController(
				starship: ActiveStarship,
				pilotName: Component,
				target: AITarget?,
				destination: Location?,
				manualWeaponSets: Set<AIShipConfiguration.AIStarshipTemplate.WeaponSet>,
				autoWeaponSets: Set<AIShipConfiguration.AIStarshipTemplate.WeaponSet>,
				previousController: AIController?
			): AIController {
				return AIController(
					starship,
					"CORVETTE",
					AIShipDamager(starship),
					pilotName,
					manualWeaponSets,
					autoWeaponSets
				).apply {
					this.starship.updatePower(name, 10, 50, 40)

					val targeting = ClosestTargetingEngine(this, 5000.0, target).apply { sticky = false }

					engines["targeting"] = targeting
					val combatEngine = StarfighterCombatEngine(this, targeting::findTarget).apply { shouldFaceTarget = true }
					engines["combat"] = combatEngine
					engines["aggro"] = AggroUponDamageEngine(this, combatEngine)

					val positioning = StandoffPositioningEngine(this, targeting::findTarget, 40.0)
					engines["positioning"] = positioning

//					val pathfinding = CombatAStarPathfindingEngine(this, positioning::findPositionVec3i)
					val pathfinding = SteeringPathfindingEngine(this, positioning::findPositionVec3i)
					engines["pathfinding"] = pathfinding
					engines["movement"] = CruiseEngine(
						this,
						pathfinding,
						pathfinding::getDestination,
						CruiseEngine.ShiftFlightType.ALL,
						256.0
					)
				}
			}
		}
	)

	val FRIGATE = registerFactory(
		"FRIGATE",
		object : AIControllerFactory("FRIGATE") {
			override fun createController(
				starship: ActiveStarship,
				pilotName: Component,
				target: AITarget?,
				destination: Location?,
				manualWeaponSets: Set<AIShipConfiguration.AIStarshipTemplate.WeaponSet>,
				autoWeaponSets: Set<AIShipConfiguration.AIStarshipTemplate.WeaponSet>,
				previousController: AIController?
			): AIController {
				return AIController(
					starship,
					"FRIGATE",
					AIShipDamager(starship),
					pilotName,
					manualWeaponSets,
					autoWeaponSets
				).apply {
					val targeting = ClosestTargetingEngine(this, 5000.0, target).apply { sticky = true }

					engines["targeting"] = targeting
					val combatEngine = FrigateCombatEngine(this, targeting::findTarget).apply { shouldFaceTarget = false }
					engines["combat"] = combatEngine
					engines["aggro"] = AggroUponDamageEngine(this, combatEngine)

					val positioning = CirclingPositionEngine(this, targeting::findTarget, 240.0)
					engines["positioning"] = positioning

//					val pathfinding = CombatAStarPathfindingEngine(this, positioning::findPositionVec3i)
					val pathfinding = SteeringPathfindingEngine(this, positioning::findPositionVec3i)
					engines["pathfinding"] = pathfinding
					engines["movement"] = CruiseEngine(
						this,
						pathfinding,
						pathfinding::getDestination,
						CruiseEngine.ShiftFlightType.ALL,
						256.0
					)
				}
			}
		}
	)

	val TEMPORARY_FRIGATE = registerFactory(
		"TEMPORARY_FRIGATE",
		object : AIControllerFactory("FRIGATE") {
			override fun createController(
				starship: ActiveStarship,
				pilotName: Component,
				target: AITarget?,
				destination: Location?,
				manualWeaponSets: Set<AIShipConfiguration.AIStarshipTemplate.WeaponSet>,
				autoWeaponSets: Set<AIShipConfiguration.AIStarshipTemplate.WeaponSet>,
				previousController: AIController?
			): AIController {
				return AIController(
					starship,
					"TEMPORARY_FRIGATE",
					AIShipDamager(starship),
					pilotName,
					manualWeaponSets,
					autoWeaponSets,
				).apply {
					val targeting = ClosestTargetingEngine(this, 5000.0, target).apply { sticky = true }

					engines["targeting"] = targeting
					engines["combat"] = FrigateCombatEngine(this, targeting::findTarget).apply { shouldFaceTarget = false }

					val positioning = CirclingPositionEngine(this, targeting::findTarget, 240.0)
					engines["positioning"] = positioning

//					val pathfinding = CombatAStarPathfindingEngine(this, positioning::findPositionVec3i)
					val pathfinding = SteeringPathfindingEngine(this, positioning::findPositionVec3i)
					engines["pathfinding"] = pathfinding
					engines["movement"] = CruiseEngine(
						this,
						pathfinding,
						pathfinding::getDestination,
						CruiseEngine.ShiftFlightType.ALL,
						256.0
					)

					engines["fallback"] = TemporaryControllerEngine(this, previousController!!)
				}
			}
		}
	)

//	val CRUISE_FRIGATE_FALLBACK = registerFactory(
//		"STARFIGHTER",
//		object : AIControllerFactory("STARFIGHTER") {
//			override fun createController(
//				starship: ActiveStarship,
//				pilotName: Component,
//				target: AITarget?,
//				destination: Location?,
//				aggressivenessLevel: AggressivenessLevel,
//				manualWeaponSets: Set<AIShipConfiguration.AIStarshipTemplate.WeaponSet>,
//				autoWeaponSets: Set<AIShipConfiguration.AIStarshipTemplate.WeaponSet>,
//				previousController: AIController?
//			): AutoCruiseAIController {
//				return AutoCruiseAIController(
//					starship,
//					destination!!,
//					-1,
//					aggressivenessLevel,
//					pilotName,
//					FRIGATE
//				)
//			}
//		}
//	)

	operator fun get(identifier: String) = presetControllers[identifier]!!

	fun registerFactory(identifier: String, factory: AIControllerFactory): AIControllerFactory {
		presetControllers[identifier] = factory
		return factory
	}

	abstract class AIControllerFactory(val identifier: String) {
		abstract fun createController(
			starship: ActiveStarship,
			pilotName: Component,
			target: AITarget?,
			destination: Location?,
			manualWeaponSets: Set<AIShipConfiguration.AIStarshipTemplate.WeaponSet>,
			autoWeaponSets: Set<AIShipConfiguration.AIStarshipTemplate.WeaponSet>,
			previousController: AIController?
		): AIController
	}
}
