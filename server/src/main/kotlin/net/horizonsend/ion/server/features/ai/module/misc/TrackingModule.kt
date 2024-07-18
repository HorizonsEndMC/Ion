package net.horizonsend.ion.server.features.ai.module.misc

import net.horizonsend.ion.common.utils.miscellaneous.squared
import net.horizonsend.ion.server.features.ai.util.AITarget
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.getDirection
import org.apache.commons.collections4.queue.CircularFifoQueue
import org.bukkit.util.Vector
import java.util.function.Supplier
import kotlin.math.cos
import kotlin.math.sqrt

class TrackingModule(
    controller: AIController,
    private val trackDuration: Int,
    private val projectileVelocity: Double,
    private val targetingSupplier: Supplier<AITarget?>
) : net.horizonsend.ion.server.features.ai.module.AIModule(controller) {
    private val lastMovements = CircularFifoQueue<Vec3i>(trackDuration)
    private var offsetVector = Vector()
    private var ticks = 0

    override fun tick() {
        ticks++

        // Only update last movements every second
        if (ticks % 20 == 0) {
            val target = targetingSupplier.get()

            if (target == null) {
                lastMovements.clear()
                return
            }

            // record current position vector for velocity prediction
            lastMovements.add(target.getVec3i())

            // Tracking algo from http://officialtwelve.blogspot.com/2015/08/projectile-interception.html
            val averageVelocity = getAverageVelocityVector()
            val direction = getDirection(Vec3i(getCenter()), target.getVec3i(false))
            val angle = direction.angle(averageVelocity)

            val a = averageVelocity.lengthSquared() - projectileVelocity.squared()
            val b = -2 * cos(angle) * direction.length() * averageVelocity.length()
            val c = direction.length().squared()
            val delta = sqrt(b.squared() - (4 * a * c))
            val t = if (a != 0.0) -(b + delta) / (2 * a) else 0.0

            offsetVector = averageVelocity.clone().multiply(t)
        }
    }

    fun findTarget(): AITarget? {
        val target = targetingSupplier.get()
        target?.offset = Vec3i(offsetVector.x.toInt(), offsetVector.y.toInt(), offsetVector.z.toInt())
        return target
    }

    private fun getAverageVelocityVector(): Vector {
        // need at least two recorded locations for calculation
        if (lastMovements.count() < 2) return Vector()

        val movementSum = Vector()
        for (v in 1 until lastMovements.size) {
            val direction = getDirection(lastMovements[v - 1], lastMovements[v])
            movementSum.add(direction)
        }

        return movementSum.multiply(1.0 / trackDuration.toDouble())
    }
}