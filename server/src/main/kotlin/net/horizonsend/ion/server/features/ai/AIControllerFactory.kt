package net.horizonsend.ion.server.features.ai

import net.horizonsend.ion.server.features.ai.configuration.AIStarshipTemplate.WeaponSet
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.damager.AIShipDamager
import net.kyori.adventure.text.Component
import java.util.function.Supplier

class AIControllerFactory private constructor(
	val identifier: String,
	private val name: String,
	private val modules: (AIController) -> Builder.ModuleBuilder
) {
	/** Build the controller */
	operator fun invoke(
		starship: ActiveStarship,
		pilotName: Component,
		autoSets: Set<WeaponSet>,
		manualSets: Set<WeaponSet>
	) : AIController {
		return AIController(
			starship = starship,
			name = name,
			damager = AIShipDamager(starship),
			pilotName = pilotName,
			manualWeaponSets = autoSets,
			autoWeaponSets = manualSets,
			createModules = modules
		)
	}

	operator fun invoke(starship: ActiveStarship, pilotName: Component) = invoke(starship, pilotName, setOf(), setOf())

	class Builder(val identifier: String) {
		private var name: String = "AI_Controller"
		private var modules: (AIController) -> ModuleBuilder = { ModuleBuilder() }

		constructor(identifier: String, factory: AIControllerFactory) : this(identifier) {
			name = factory.name
			modules = factory.modules
		}

		fun setControllerTypeName(name: String) = apply { this.name = name }

		fun setModuleBuilder(moduleBuilder: (AIController) -> ModuleBuilder) = apply { modules = moduleBuilder }

		fun build(): AIControllerFactory = AIControllerFactory(identifier, name, modules)

		class ModuleBuilder {
			private val modules: MutableMap<String, net.horizonsend.ion.server.features.ai.module.AIModule> = mutableMapOf()

			fun <T: net.horizonsend.ion.server.features.ai.module.AIModule> suppliedModule(identifier: String, modification: (T) -> T = { it }): Supplier<T> = Supplier {
				@Suppress("UNCHECKED_CAST") // Up to the user to make sure of that
				val module = modules[identifier] as T

				modification(module)
			}

			fun <T: net.horizonsend.ion.server.features.ai.module.AIModule> addModule(name: String, module: T): T {
				modules[name] = module
				return module
			}

			fun build(): Map<String, net.horizonsend.ion.server.features.ai.module.AIModule> = modules
		}
	}
}
