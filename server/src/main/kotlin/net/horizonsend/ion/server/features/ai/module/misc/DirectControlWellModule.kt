package net.horizonsend.ion.server.features.ai.module.misc

import net.horizonsend.ion.common.extensions.userErrorAction
import net.horizonsend.ion.server.features.ai.util.AITarget
import net.horizonsend.ion.server.features.ai.util.StarshipTarget
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import java.util.function.Supplier

class DirectControlWellModule(
    controller: AIController,
    private val range: Double,
    private val targetingSupplier: Supplier<List<AITarget>>
) : net.horizonsend.ion.server.features.ai.module.AIModule(controller) {
    private var ticks = 0
    override fun tick() {
        ticks++

        val targets = targetingSupplier.get()

        // every second
        if (ticks % 20 == 0) {
            for (target in targets) {
                if (controller.getCenter().distance(target.getVec3i()) > range) continue
                if (target !is StarshipTarget) continue

                val starship = target.ship
                if (starship !is ActiveControlledStarship) continue

                if (starship.isDirectControlEnabled) {
                    starship.setDirectControlEnabled(false)
                    starship.userErrorAction("${controller.starship.getDisplayNamePlain()} disabled Direct Control!")
                }
            }
        }
    }
}