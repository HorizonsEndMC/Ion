package net.horizonsend.ion.server.features.starship.active.ai

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.configuration.AIShipConfiguration
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ai.util.AITarget
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.combat.FrigateCombatAIController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.combat.StarfighterCombatAIController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.combat.TemporaryFrigateCombatController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.combat.TemporaryStarfighterCombatAIController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.navigation.AutoCruiseAIController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.utils.AggressivenessLevel
import net.kyori.adventure.text.Component
import org.bukkit.Location

object AIControllers : IonServerComponent() {
	val presetControllers = mutableMapOf<String, AIControllerFactory<*>>()

	val STARFIGHTER = registerFactory(
		"STARFIGHTER",
		object : AIControllerFactory<StarfighterCombatAIController>("STARFIGHTER") {
			override fun createController(
				starship: ActiveStarship,
				pilotName: Component,
				target: AITarget?,
				destination: Location?,
				aggressivenessLevel: AggressivenessLevel,
				manualWeaponSets: Set<AIShipConfiguration.AIStarshipTemplate.WeaponSet>,
				autoWeaponSets: Set<AIShipConfiguration.AIStarshipTemplate.WeaponSet>,
				previousController: AIController?
			): StarfighterCombatAIController {
				return StarfighterCombatAIController(
					starship,
					target,
					pilotName,
					aggressivenessLevel
				)
			}
		}
	)

	val TEMPORARY_STARFIGHTER = registerFactory(
		"STARFIGHTER",
		object : AIControllerFactory<TemporaryStarfighterCombatAIController>("STARFIGHTER") {
			override fun createController(
				starship: ActiveStarship,
				pilotName: Component,
				target: AITarget?,
				destination: Location?,
				aggressivenessLevel: AggressivenessLevel,
				manualWeaponSets: Set<AIShipConfiguration.AIStarshipTemplate.WeaponSet>,
				autoWeaponSets: Set<AIShipConfiguration.AIStarshipTemplate.WeaponSet>,
				previousController: AIController?
			): TemporaryStarfighterCombatAIController {
				return TemporaryStarfighterCombatAIController(
					target,
					previousController!!
				)
			}
		}
	)

	val CRUISE_STARFIGHTER_FALLBACK = registerFactory(
		"STARFIGHTER",
		object : AIControllerFactory<AutoCruiseAIController>("STARFIGHTER") {
			override fun createController(
				starship: ActiveStarship,
				pilotName: Component,
				target: AITarget?,
				destination: Location?,
				aggressivenessLevel: AggressivenessLevel,
				manualWeaponSets: Set<AIShipConfiguration.AIStarshipTemplate.WeaponSet>,
				autoWeaponSets: Set<AIShipConfiguration.AIStarshipTemplate.WeaponSet>,
				previousController: AIController?
			): AutoCruiseAIController {
				return AutoCruiseAIController(
					starship,
					destination!!,
					-1,
					aggressivenessLevel,
					pilotName,
					STARFIGHTER
				)
			}
		}
	)

	val FRIGATE = registerFactory(
		"FRIGATE",
		object : AIControllerFactory<FrigateCombatAIController>("FRIGATE") {
			override fun createController(
				starship: ActiveStarship,
				pilotName: Component,
				target: AITarget?,
				destination: Location?,
				aggressivenessLevel: AggressivenessLevel,
				manualWeaponSets: Set<AIShipConfiguration.AIStarshipTemplate.WeaponSet>,
				autoWeaponSets: Set<AIShipConfiguration.AIStarshipTemplate.WeaponSet>,
				previousController: AIController?
			): FrigateCombatAIController {
				return FrigateCombatAIController(
					starship,
					target,
					pilotName,
					aggressivenessLevel,
					manualWeaponSets,
					autoWeaponSets
				)
			}
		}
	)

	val TEMPORARY_FRIGATE = registerFactory(
		"STARFIGHTER",
		object : AIControllerFactory<TemporaryFrigateCombatController>("STARFIGHTER") {
			override fun createController(
				starship: ActiveStarship,
				pilotName: Component,
				target: AITarget?,
				destination: Location?,
				aggressivenessLevel: AggressivenessLevel,
				manualWeaponSets: Set<AIShipConfiguration.AIStarshipTemplate.WeaponSet>,
				autoWeaponSets: Set<AIShipConfiguration.AIStarshipTemplate.WeaponSet>,
				previousController: AIController?
			): TemporaryFrigateCombatController {
				return TemporaryFrigateCombatController(
					previousController!!,
					target!!,
					manualWeaponSets,
					autoWeaponSets
				)
			}
		}
	)

	val CRUISE_FRIGATE_FALLBACK = registerFactory(
		"STARFIGHTER",
		object : AIControllerFactory<AutoCruiseAIController>("STARFIGHTER") {
			override fun createController(
				starship: ActiveStarship,
				pilotName: Component,
				target: AITarget?,
				destination: Location?,
				aggressivenessLevel: AggressivenessLevel,
				manualWeaponSets: Set<AIShipConfiguration.AIStarshipTemplate.WeaponSet>,
				autoWeaponSets: Set<AIShipConfiguration.AIStarshipTemplate.WeaponSet>,
				previousController: AIController?
			): AutoCruiseAIController {
				return AutoCruiseAIController(
					starship,
					destination!!,
					-1,
					aggressivenessLevel,
					pilotName,
					FRIGATE
				)
			}
		}
	)

	operator fun get(identifier: String) = presetControllers[identifier]!!

	fun <T: AIController> registerFactory(identifier: String, factory: AIControllerFactory<T>): AIControllerFactory<T> {
		presetControllers[identifier] = factory
		return factory
	}

	abstract class AIControllerFactory<T: AIController>(val identifier: String) {
		abstract fun createController(
			starship: ActiveStarship,
			pilotName: Component,
			target: AITarget?,
			destination: Location?,
			aggressivenessLevel: AggressivenessLevel,
			manualWeaponSets: Set<AIShipConfiguration.AIStarshipTemplate.WeaponSet>,
			autoWeaponSets: Set<AIShipConfiguration.AIStarshipTemplate.WeaponSet>,
			previousController: AIController?
		): T
	}
}
