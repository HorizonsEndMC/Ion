package net.horizonsend.ion.server.features.ai.spawning.ships

import net.horizonsend.ion.server.features.ai.AIControllerFactories
import net.horizonsend.ion.server.features.ai.configuration.AITemplate
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.kyori.adventure.text.Component
import org.bukkit.util.Vector
import org.slf4j.Logger
import java.util.function.Supplier

data class GroupSpawnedShip(
    override val template: AITemplate,
    val nameProvider: Supplier<Component>,
    val controllerModifier: AIController.() -> Unit = {},
) : SpawnedShip {
	override val offsets: MutableList<Supplier<Vector>> = mutableListOf()
    override var absoluteHeight: Double? = null

    override fun createController(logger: Logger, starship: ActiveStarship, difficulty: Int): AIController {
        val factory = AIControllerFactories[template.behaviorInformation.controllerFactory]

        val controller = factory.invoke(
			starship,
			getName(difficulty),
			template.starshipInfo.autoWeaponSets,
			template.starshipInfo.manualWeaponSets,
			difficulty
		)

        controllerModifier.invoke(controller)

        return controller
    }

    override fun getName(difficulty: Int): Component {
        return nameProvider.get()
    }
}
