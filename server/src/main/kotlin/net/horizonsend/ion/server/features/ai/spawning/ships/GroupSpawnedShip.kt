package net.horizonsend.ion.server.features.ai.spawning.ships

import net.horizonsend.ion.server.features.ai.AIControllerFactories
import net.horizonsend.ion.server.features.ai.configuration.AITemplate
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.kyori.adventure.text.Component
import org.slf4j.Logger
import java.util.function.Supplier

data class GroupSpawnedShip(
    override val template: AITemplate,
    val nameProvider: Supplier<Component>,
    val controllerModifier: AIController.() -> Unit = {},
) : SpawnedShip {
    override fun createController(logger: Logger, starship: ActiveStarship): AIController {
        val factory = AIControllerFactories[template.behaviorInformation.controllerFactory]

        val controller = factory.invoke(starship, getName(logger))
        controllerModifier.invoke(controller)

        return controller
    }

    override fun getName(logger: Logger): Component {
        return nameProvider.get()
    }
}
