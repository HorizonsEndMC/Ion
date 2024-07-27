package net.horizonsend.ion.server.features.ai.module.targeting

import net.horizonsend.ion.server.features.ai.util.AITarget
import net.horizonsend.ion.server.features.ai.util.StarshipTarget
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.damager.Damager

class ClosestSmallStarshipTargetingModule(
    controller: AIController,
    var maxRange: Double,
    existingTarget: AITarget? = null
) : TargetingModule(controller) {
    private var lastDamaged: Long = 0

    init {
        lastTarget = existingTarget
    }

    override fun onDamaged(damager: Damager) {
        lastDamaged = System.currentTimeMillis()
        if (lastTarget == null) lastTarget = damager.getAITarget()
    }

    override fun searchForTarget(): AITarget? {
        if (lastTarget != null && lastDamaged >= System.currentTimeMillis() - 5000) return lastTarget

        return searchForTargetList().firstOrNull()
    }

    override fun searchForTargetList(): List<AITarget> {
        return controller.getNearbyTargetsInRadius(0.0, maxRange) {
            if (it is StarshipTarget) {
                it.ship.controller !is AIController
            } else true
        }.sortedWith(
            Comparator { o1, o2 ->
                // if both objects are not StarshipTargets, maintain order
                // if only object 1 is not a StarshipTarget, object 1 should appear after object 2
                if (o1 !is StarshipTarget) if (o2 !is StarshipTarget) return@Comparator 0 else return@Comparator 1

                // if only object 2 is not a StarshipTarget, object 1 should appear before object 2
                if (o2 !is StarshipTarget) return@Comparator -1

                val type1 = o1.ship.type
                val type2 = o2.ship.type

                if (!sortMap.containsKey(type1)) if (!sortMap.containsKey(type2)) return@Comparator 0 else return@Comparator 1
                if (!sortMap.containsKey(type2)) return@Comparator -1

                return@Comparator sortMap[type1]!! - sortMap[type2]!!
            }
        )
    }

    private val sortMap = mapOf(
        StarshipType.STARFIGHTER to 0,
        StarshipType.GUNSHIP to 1,
        StarshipType.CORVETTE to 2,
        StarshipType.SHUTTLE to 0,
        StarshipType.TRANSPORT to 1,
        StarshipType.LIGHT_FREIGHTER to 2
    )
}