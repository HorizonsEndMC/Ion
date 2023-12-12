package net.horizonsend.ion.server.features.starship.active.ai

import net.horizonsend.ion.server.configuration.AIShipConfiguration.AIStarshipTemplate.WeaponSet
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ai.module.AIModule
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.damager.AIShipDamager
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.kyori.adventure.text.Component
import java.util.function.Supplier

class AIControllerFactory private constructor(
	private val name: String,
	private val modules: (AIController) -> Builder.ModuleBuilder,
	private val vec3iSupplier: Supplier<Vec3i>
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
		private var vec3iSupplier: Supplier<Vec3i> = Supplier { Vec3i(0, 0, 0) }
		private var modules: (AIController) -> ModuleBuilder = { ModuleBuilder() }

		fun setControllerTypeName(name: String) = apply { this.name = name }

		fun setModuleBuilder(moduleBuilder: (AIController) -> ModuleBuilder) = apply { modules = moduleBuilder }

		fun addLocationSupplier(supplier: Supplier<Vec3i>) = apply { vec3iSupplier = supplier }

		fun build(): AIControllerFactory = AIControllerFactory(name, modules, vec3iSupplier)

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
