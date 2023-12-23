package net.horizonsend.ion.server.features.starship.ai

import net.horizonsend.ion.server.configuration.AIShipConfiguration.AIStarshipTemplate.WeaponSet
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.ai.module.AIModule
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.damager.AIShipDamager
import net.kyori.adventure.text.Component
import org.bukkit.Location
import java.util.Optional

class AIControllerFactory private constructor(
	private val name: String,
	private val modules: (AIController) -> Builder.ModuleBuilder,
	private val locationSupplier: (AIController) -> Optional<Location>
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

	class Builder {
		private var name: String = "AI_Controller"
		private var locationSupplier: (AIController) -> Optional<Location> = { Optional.empty() }
		private var modules: (AIController) -> ModuleBuilder = { ModuleBuilder() }

		constructor()

		constructor(factory: AIControllerFactory) {
			name = factory.name
			locationSupplier = factory.locationSupplier
			modules = factory.modules
		}

		fun setControllerTypeName(name: String) = apply { this.name = name }

		fun setModuleBuilder(moduleBuilder: (AIController) -> ModuleBuilder) = apply { modules = moduleBuilder }

		fun addLocationSupplier(supplier: (AIController) -> Optional<Location>) = apply { locationSupplier = supplier }

		fun getLocationSupplier() = locationSupplier

		fun build(): AIControllerFactory = AIControllerFactory(name, modules, locationSupplier)

		class ModuleBuilder {
			private val modules: MutableMap<String, AIModule> = mutableMapOf()

			fun <T: AIModule> addModule(name: String, module: T): T {
				modules[name] = module
				return module
			}

			fun build(): Map<String, AIModule> = modules
		}
	}
}
