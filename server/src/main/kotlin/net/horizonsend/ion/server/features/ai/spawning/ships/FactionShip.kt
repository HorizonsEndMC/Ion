package net.horizonsend.ion.server.features.ai.spawning.ships

import net.horizonsend.ion.server.features.ai.AIControllerFactories
import net.horizonsend.ion.server.features.ai.configuration.AITemplate
import net.horizonsend.ion.server.features.ai.faction.AIFaction
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.kyori.adventure.text.Component
import org.bukkit.util.Vector
import org.slf4j.Logger
import java.util.function.Supplier

class FactionShip(
	override val template: AITemplate,
	private val faction: AIFaction
) : SpawnedShip {
	override val offsets: MutableList<Supplier<Vector>> = mutableListOf()
	override var absoluteHeight: Double? = null
	override var pilotName : Component? = null

	override fun createController(logger: Logger, starship: ActiveStarship, difficulty: Int): AIController {
		val factory = AIControllerFactories[template.behaviorInformation.controllerFactory]

		val controller = factory.invoke(
			starship,
			getName(difficulty),
			template.starshipInfo.autoWeaponSets,
			template.starshipInfo.manualWeaponSets,
			difficulty
		)

		faction.controllerModifier.invoke(controller)

		return controller
	}

	override fun getName(difficulty: Int): Component {
		if (pilotName == null) {
			pilotName = faction.getAvailableName(difficulty)
		}
		return pilotName!!
	}
}
