package net.horizonsend.ion.server.features.ai.module.targeting

import net.horizonsend.ion.server.features.ai.util.AITarget
import net.horizonsend.ion.server.features.ai.util.StarshipTarget
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.damager.Damager

class ClosestLargeStarshipTargetingModule(
    controller: AIController,
    var maxRange: Double,
    existingTarget: AITarget? = null,
    private val targetAI: Boolean = false,
    private val focusRange: Double = 0.0
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
        return searchForTargetList().firstOrNull()
    }

    override fun searchForTargetList(): List<AITarget> {
        val map = if (!targetAI) sortMap else aiSortMap

        return controller.getNearbyTargetsInRadius(0.0, maxRange) {
            if (it is StarshipTarget) {
                if (!targetAI) it.ship.controller !is AIController else it.ship.controller is AIController && starship.controller != it.ship.controller
            } else true
        }.sortedWith(
            Comparator<AITarget> { o1, o2 ->
                // if both objects are not StarshipTargets, maintain order
                // if only object 1 is not a StarshipTarget, object 1 should appear after object 2
                if (o1 !is StarshipTarget) if (o2 !is StarshipTarget) return@Comparator 0 else return@Comparator 1

                // if only object 2 is not a StarshipTarget, object 1 should appear before object 2
                if (o2 !is StarshipTarget) return@Comparator -1

                val type1 = o1.ship.type
                val type2 = o2.ship.type

                if (!map.containsKey(type1)) if (!map.containsKey(type2)) return@Comparator 0 else return@Comparator 1
                if (!map.containsKey(type2)) return@Comparator -1

                return@Comparator map[type1]!! - map[type2]!!
            }.thenComparing(Comparator<AITarget> { o1, o2 ->
                // compare by distance within focusRange
                val distance1 = controller.getCenter().distance(o1.getVec3i())
                val distance2 = controller.getCenter().distance(o2.getVec3i())

                if (distance1 > focusRange) if (distance2 > focusRange) return@Comparator 0 else return@Comparator 1
                if (distance2 > focusRange) return@Comparator -1

                return@Comparator distance1.compareTo(distance2)
            })
        )
    }

    private val sortMap = mapOf(
        StarshipType.BATTLECRUISER to 0,
        StarshipType.CRUISER to 1,
        StarshipType.DESTROYER to 2,
        StarshipType.FRIGATE to 3,
        StarshipType.BARGE to 0,
        StarshipType.HEAVY_FREIGHTER to 2,
        StarshipType.MEDIUM_FREIGHTER to 3
    )

    private val aiSortMap = mapOf(
        StarshipType.AI_BATTLECRUISER to 0,
        StarshipType.AI_CRUISER to 1,
        StarshipType.AI_DESTROYER to 2,
        StarshipType.AI_FRIGATE to 3,
        StarshipType.AI_BARGE to 0,
        StarshipType.AI_HEAVY_FREIGHTER to 2
    )
}