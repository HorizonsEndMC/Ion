package net.horizonsend.ion.server.features.starship.active.ai

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.configuration.AIShipConfiguration
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ai.module.combat.FrigateCombatModule
import net.horizonsend.ion.server.features.starship.active.ai.module.combat.StarfighterCombatModule
import net.horizonsend.ion.server.features.starship.active.ai.module.misc.AggroUponDamageModule
import net.horizonsend.ion.server.features.starship.active.ai.module.misc.CombatModeModule
import net.horizonsend.ion.server.features.starship.active.ai.module.misc.TemporaryControllerModule
import net.horizonsend.ion.server.features.starship.active.ai.module.movement.CruiseModule
import net.horizonsend.ion.server.features.starship.active.ai.module.pathfinding.SteeringPathfindingModule
import net.horizonsend.ion.server.features.starship.active.ai.module.positioning.AxisStandoffPositioningModule
import net.horizonsend.ion.server.features.starship.active.ai.module.positioning.BasicPositioningModule
import net.horizonsend.ion.server.features.starship.active.ai.module.positioning.CirclingPositionModule
import net.horizonsend.ion.server.features.starship.active.ai.module.positioning.StandoffPositioningModule
import net.horizonsend.ion.server.features.starship.active.ai.module.targeting.ClosestTargetingModule
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
					val targeting = ClosestTargetingModule(this, 5000.0, target).apply { sticky = false }

					modules["targeting"] = targeting
					modules["combat"] = StarfighterCombatModule(this, targeting::findTarget)

					val positioning = AxisStandoffPositioningModule(this, targeting::findTarget, 25.0)
					modules["positioning"] = positioning

//					val pathfinding = CombatAStarPathfindingModule(this, positioning::findPositionVec3i)
					val pathfinding = SteeringPathfindingModule(this, positioning::findPositionVec3i)
					modules["pathfinding"] = pathfinding
					modules["movement"] = CruiseModule(
						this,
						pathfinding,
						pathfinding::getDestination,
						CruiseModule.ShiftFlightType.ALL,
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
				previousController!!
				return STARFIGHTER.createController(starship, pilotName, target, destination, manualWeaponSets, autoWeaponSets, previousController).apply {
					modules["fallback"] = TemporaryControllerModule(this, previousController)
				}
			}
		}
	)

	val CRUISE_STARFIGHTER_FALLBACK = registerFactory(
		"CRUISE_STARFIGHTER_FALLBACK",
		object : AIControllerFactory("CRUISE_STARFIGHTER_FALLBACK") {
			override fun createController(
				starship: ActiveStarship,
				pilotName: Component,
				target: AITarget?,
				destination: Location?,
				manualWeaponSets: Set<AIShipConfiguration.AIStarshipTemplate.WeaponSet>,
				autoWeaponSets: Set<AIShipConfiguration.AIStarshipTemplate.WeaponSet>,
				previousController: AIController?
			): AIController {
				destination!!

				return AIController(
					starship,
					"CRUISE_STARFIGHTER_FALLBACK",
					AIShipDamager(starship),
					pilotName,
					manualWeaponSets,
					autoWeaponSets
				).apply {
					val positioning = BasicPositioningModule(this, destination)
					modules["positioning"] = positioning

					val pathfinding = SteeringPathfindingModule(this, positioning::findPositionVec3i)
					modules["pathfinding"] = pathfinding
					modules["movement"] = CruiseModule(
						this,
						pathfinding,
						pathfinding::getDestination,
						CruiseModule.ShiftFlightType.IF_BLOCKED_AND_MATCH_Y,
						256.0
					)

					modules["combatMode"] = CombatModeModule(
						this,
						TEMPORARY_STARFIGHTER,
						500.0
					) { true }
				}
			}
		}
	)

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

					val targeting = ClosestTargetingModule(this, 5000.0, target).apply { sticky = false }

					modules["targeting"] = targeting
					val combatModule = StarfighterCombatModule(this, targeting::findTarget).apply { shouldFaceTarget = true }
					modules["combat"] = combatModule
					modules["aggro"] = AggroUponDamageModule(this, combatModule)

					val positioning = StandoffPositioningModule(this, targeting::findTarget, 40.0)
					modules["positioning"] = positioning

//					val pathfinding = CombatAStarPathfindingModule(this, positioning::findPositionVec3i)
					val pathfinding = SteeringPathfindingModule(this, positioning::findPositionVec3i)
					modules["pathfinding"] = pathfinding
					modules["movement"] = CruiseModule(
						this,
						pathfinding,
						pathfinding::getDestination,
						CruiseModule.ShiftFlightType.ALL,
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
					val targeting = ClosestTargetingModule(this, 5000.0, target).apply { sticky = true }

					modules["targeting"] = targeting
					val combatModule = FrigateCombatModule(this, targeting::findTarget).apply { shouldFaceTarget = false }
					modules["combat"] = combatModule
					modules["aggro"] = AggroUponDamageModule(this, combatModule)

					val positioning = CirclingPositionModule(this, targeting::findTarget, 240.0)
					modules["positioning"] = positioning

//					val pathfinding = CombatAStarPathfindingModule(this, positioning::findPositionVec3i)
					val pathfinding = SteeringPathfindingModule(this, positioning::findPositionVec3i)
					modules["pathfinding"] = pathfinding
					modules["movement"] = CruiseModule(
						this,
						pathfinding,
						pathfinding::getDestination,
						CruiseModule.ShiftFlightType.ALL,
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
					val targeting = ClosestTargetingModule(this, 5000.0, target).apply { sticky = true }

					modules["targeting"] = targeting
					modules["combat"] = FrigateCombatModule(this, targeting::findTarget).apply { shouldFaceTarget = false }

					val positioning = CirclingPositionModule(this, targeting::findTarget, 240.0)
					modules["positioning"] = positioning

//					val pathfinding = CombatAStarPathfindingModule(this, positioning::findPositionVec3i)
					val pathfinding = SteeringPathfindingModule(this, positioning::findPositionVec3i)
					modules["pathfinding"] = pathfinding
					modules["movement"] = CruiseModule(
						this,
						pathfinding,
						pathfinding::getDestination,
						CruiseModule.ShiftFlightType.ALL,
						256.0
					)

					modules["fallback"] = TemporaryControllerModule(this, previousController!!)
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
