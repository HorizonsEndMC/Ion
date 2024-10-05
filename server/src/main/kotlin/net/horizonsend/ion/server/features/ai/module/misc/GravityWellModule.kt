package net.horizonsend.ion.server.features.ai.module.misc

import net.horizonsend.ion.server.features.ai.util.AITarget
import net.horizonsend.ion.server.features.starship.Interdiction
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.control.movement.StarshipCruising
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.getDirection
import java.util.function.Supplier

class GravityWellModule(
    controller: AIController,
    private val activeRange: Double,
    private val canWellWhileCruising: Boolean,
    private val targetingSupplier: Supplier<AITarget?>
) : net.horizonsend.ion.server.features.ai.module.AIModule(controller) {
    override fun tick() {
        val target = targetingSupplier.get()

        if (!canWellWhileCruising && (starship !is ActiveControlledStarship || StarshipCruising.isCruising(starship))) {
            starship.setIsInterdicting(false)
            return
        }

        if (target == null) {
            starship.setIsInterdicting(false)
            return
        }

        if (getDirection(Vec3i(getCenter()), target.getVec3i(false)).length() > activeRange) {
            starship.setIsInterdicting(false)
            return
        }

        val gravityWell = Interdiction.findGravityWell(starship)

        if (gravityWell == null || !gravityWell.isIntact()) {
            starship.setIsInterdicting(false)
            return
        }

        starship.setIsInterdicting(true)
    }
}
